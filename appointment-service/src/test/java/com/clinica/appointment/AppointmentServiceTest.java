package com.clinica.appointment;

import com.clinica.appointment.Dtos.AppointmentRequest;
import com.clinica.appointment.Dtos.AppointmentResponse;
import com.clinica.appointment.Dtos.CitaCompletaResponse;
import com.clinica.appointment.Dtos.OwnerDto;
import com.clinica.appointment.Dtos.PetDetalleDto;
import com.clinica.appointment.Dtos.PetDto;
import com.clinica.appointment.Errores.Conflicto;
import com.clinica.appointment.Errores.NoEncontrado;
import com.clinica.appointment.Errores.ReglaNegocio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS UNITARIAS del servicio con Mockito: se simulan el repositorio y el cliente REST,
 * por lo que solo se prueba la lógica de negocio (sin base de datos ni red).
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final LocalDate FUTURO = LocalDate.now().plusDays(7);
    private static final LocalTime HORA = LocalTime.of(10, 30);

    @Mock
    private AppointmentRepository repository;

    @Mock
    private PetClient petClient;

    @InjectMocks
    private AppointmentService service;

    private PetDto mascota() {
        return new PetDto(1L, "Firulais", "Perro", "Labrador", "MACHO", LocalDate.of(2020, 1, 1),
                20.0, "Negro", 5L, "ACTIVO");
    }

    private AppointmentRequest request(LocalDate fecha, LocalTime hora) {
        return new AppointmentRequest(1L, fecha, hora, "Vacunación", "Primera dosis", null);
    }

    private Appointment cita(Long id, Appointment.Estado estado) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setPetId(1L);
        a.setFecha(FUTURO);
        a.setHora(HORA);
        a.setMotivoConsulta("Vacunación");
        a.setEstado(estado);
        return a;
    }

    // ----- Creación de citas -----
    @Test
    void crear_conMascotaValida_quedaProgramada() {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());
        when(repository.existsByFechaAndHoraAndEstadoNot(FUTURO, HORA, Appointment.Estado.CANCELADA)).thenReturn(false);
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AppointmentResponse r = service.crear(request(FUTURO, HORA));

        assertEquals(1L, r.id());
        assertEquals(Appointment.Estado.PROGRAMADA, r.estado());
    }

    @Test
    void crear_mascotaInexistente_lanzaNoEncontrado() {
        when(petClient.obtenerMascota(1L)).thenThrow(new NoEncontrado("La mascota no existe"));

        assertThrows(NoEncontrado.class, () -> service.crear(request(FUTURO, HORA)));
        verify(repository, never()).save(any());
    }

    // ----- Validación de fechas -----
    @Test
    void crear_fechaPasada_lanzaReglaDeNegocio() {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());

        assertThrows(ReglaNegocio.class,
                () -> service.crear(request(LocalDate.now().minusDays(1), HORA)));
    }

    @Test
    void crear_mismaFechaYHora_lanzaConflicto() {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());
        when(repository.existsByFechaAndHoraAndEstadoNot(FUTURO, HORA, Appointment.Estado.CANCELADA)).thenReturn(true);

        assertThrows(Conflicto.class, () -> service.crear(request(FUTURO, HORA)));
    }

    // ----- Actualización de estados -----
    @Test
    void actualizar_cambiaEstadoAConfirmada() {
        when(repository.findById(1L)).thenReturn(Optional.of(cita(1L, Appointment.Estado.PROGRAMADA)));
        when(repository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        AppointmentRequest req = new AppointmentRequest(1L, FUTURO, HORA, "Vacunación", "Confirmada por teléfono",
                Appointment.Estado.CONFIRMADA);

        AppointmentResponse r = service.actualizar(1L, req);

        assertEquals(Appointment.Estado.CONFIRMADA, r.estado());
        verify(petClient, never()).obtenerMascota(any());
    }

    @Test
    void actualizar_citaAtendida_lanzaConflicto() {
        when(repository.findById(1L)).thenReturn(Optional.of(cita(1L, Appointment.Estado.ATENDIDA)));

        assertThrows(Conflicto.class, () -> service.actualizar(1L, request(FUTURO, HORA)));
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_nuevoHorarioOcupado_lanzaConflicto() {
        LocalTime otraHora = LocalTime.of(15, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(cita(1L, Appointment.Estado.PROGRAMADA)));
        when(repository.existsByFechaAndHoraAndEstadoNotAndIdNot(FUTURO, otraHora, Appointment.Estado.CANCELADA, 1L))
                .thenReturn(true);

        assertThrows(Conflicto.class, () -> service.actualizar(1L, request(FUTURO, otraHora)));
    }

    // ----- Eliminación -----
    @Test
    void eliminar_citaAtendida_lanzaConflicto() {
        when(repository.findById(1L)).thenReturn(Optional.of(cita(1L, Appointment.Estado.ATENDIDA)));

        assertThrows(Conflicto.class, () -> service.eliminar(1L));
        verify(repository, never()).delete(any(Appointment.class));
    }

    // ----- Consulta completa (integración Appointment -> Pet -> Owner) -----
    @Test
    void consultarCompleta_combinaCitaMascotaYPropietario() {
        OwnerDto propietario = new OwnerDto(5L, "Ana", "Pérez", "1001", "3001234567", "ana@mail.com",
                null, LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(cita(1L, Appointment.Estado.PROGRAMADA)));
        when(petClient.obtenerDetalle(1L)).thenReturn(new PetDetalleDto(mascota(), propietario));

        CitaCompletaResponse r = service.consultarCompleta(1L);

        assertEquals(1L, r.cita().id());
        assertEquals("Firulais", r.mascota().nombre());
        assertEquals("Ana", r.propietario().nombre());
    }
}
