package com.clinica.appointment;

import com.clinica.appointment.Dtos.AppointmentRequest;
import com.clinica.appointment.Dtos.AppointmentResponse;
import com.clinica.appointment.Dtos.CitaCompletaResponse;
import com.clinica.appointment.Dtos.PetDetalleDto;
import com.clinica.appointment.Errores.Conflicto;
import com.clinica.appointment.Errores.NoEncontrado;
import com.clinica.appointment.Errores.ReglaNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Lógica de negocio de citas. Reglas:
 *  - La mascota debe existir (se consulta a Pet-Service) -> si no, 404.
 *  - No se permiten fechas/horas pasadas -> 400.
 *  - No puede haber dos citas (no canceladas) en la misma fecha y hora -> 409.
 *  - Una cita ATENDIDA no se puede modificar ni eliminar -> 409.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PetClient petClient;

    public AppointmentService(AppointmentRepository repository, PetClient petClient) {
        this.repository = repository;
        this.petClient = petClient;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listar() {
        return repository.findAll().stream().map(AppointmentResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse obtener(Long id) {
        return AppointmentResponse.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listarPorMascota(Long petId) {
        return repository.findByPetId(petId).stream().map(AppointmentResponse::desde).toList();
    }

    /** Cita + mascota + propietario: Appointment -> Pet -> Owner. */
    @Transactional(readOnly = true)
    public CitaCompletaResponse consultarCompleta(Long id) {
        Appointment cita = buscar(id);
        PetDetalleDto detalle = petClient.obtenerDetalle(cita.getPetId());
        return new CitaCompletaResponse(AppointmentResponse.desde(cita), detalle.mascota(), detalle.propietario());
    }

    @Transactional
    public AppointmentResponse crear(AppointmentRequest req) {
        petClient.obtenerMascota(req.petId()); // lanza 404 si la mascota no existe
        validarFechaFutura(req.fecha(), req.hora());
        if (repository.existsByFechaAndHoraAndEstadoNot(req.fecha(), req.hora(), Appointment.Estado.CANCELADA)) {
            throw new Conflicto("Ya existe una cita programada para el " + req.fecha() + " a las " + req.hora());
        }
        Appointment cita = new Appointment();
        copiar(req, cita);
        cita.setEstado(Appointment.Estado.PROGRAMADA); // toda cita nueva nace PROGRAMADA
        return AppointmentResponse.desde(repository.save(cita));
    }

    @Transactional
    public AppointmentResponse actualizar(Long id, AppointmentRequest req) {
        Appointment cita = buscar(id);
        if (cita.getEstado() == Appointment.Estado.ATENDIDA) {
            throw new Conflicto("No se puede modificar una cita que ya fue atendida");
        }
        // Solo se consulta a Pet-Service si cambió la mascota
        if (!cita.getPetId().equals(req.petId())) {
            petClient.obtenerMascota(req.petId());
        }
        // Solo se revisa el horario si cambió la fecha o la hora
        boolean cambioHorario = !cita.getFecha().equals(req.fecha()) || !cita.getHora().equals(req.hora());
        if (cambioHorario) {
            validarFechaFutura(req.fecha(), req.hora());
            if (repository.existsByFechaAndHoraAndEstadoNotAndIdNot(
                    req.fecha(), req.hora(), Appointment.Estado.CANCELADA, id)) {
                throw new Conflicto("Ya existe una cita programada para el " + req.fecha() + " a las " + req.hora());
            }
        }
        copiar(req, cita);
        if (req.estado() != null) {
            cita.setEstado(req.estado());
        }
        return AppointmentResponse.desde(repository.save(cita));
    }

    @Transactional
    public void eliminar(Long id) {
        Appointment cita = buscar(id);
        if (cita.getEstado() == Appointment.Estado.ATENDIDA) {
            throw new Conflicto("No se puede eliminar una cita que ya fue atendida");
        }
        repository.delete(cita);
    }

    /** Busca por id o lanza 404. */
    private Appointment buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoEncontrado("Cita con id " + id + " no encontrada"));
    }

    /** Regla: la fecha y hora de la cita no pueden estar en el pasado. */
    private void validarFechaFutura(LocalDate fecha, LocalTime hora) {
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new ReglaNegocio("No se pueden programar citas en fechas u horas pasadas");
        }
    }

    private void copiar(AppointmentRequest req, Appointment cita) {
        cita.setPetId(req.petId());
        cita.setFecha(req.fecha());
        cita.setHora(req.hora());
        cita.setMotivoConsulta(req.motivoConsulta().trim());
        cita.setObservaciones(req.observaciones());
    }
}
