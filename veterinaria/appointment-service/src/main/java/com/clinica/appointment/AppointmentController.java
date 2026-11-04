package com.clinica.appointment;

import com.clinica.appointment.Dtos.AppointmentRequest;
import com.clinica.appointment.Dtos.AppointmentResponse;
import com.clinica.appointment.Dtos.CitaCompletaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** API REST de citas (ruta base /appointments). Solo recibe, valida y delega en AppointmentService. */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    /** GET /appointments -> todas las citas. */
    @GetMapping
    public List<AppointmentResponse> listar() {
        return service.listar();
    }

    /** GET /appointments/{id} -> una cita (200) o 404. */
    @GetMapping("/{id}")
    public AppointmentResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** GET /appointments/{id}/completa -> cita + mascota + propietario. */
    @GetMapping("/{id}/completa")
    public CitaCompletaResponse consultarCompleta(@PathVariable Long id) {
        return service.consultarCompleta(id);
    }

    /** GET /appointments/pet/{petId} -> citas de una mascota. */
    @GetMapping("/pet/{petId}")
    public List<AppointmentResponse> listarPorMascota(@PathVariable Long petId) {
        return service.listarPorMascota(petId);
    }

    /** POST /appointments -> 201; 404 si la mascota no existe; 400 fecha pasada; 409 horario ocupado. */
    @PostMapping
    public ResponseEntity<AppointmentResponse> crear(@Valid @RequestBody AppointmentRequest req) {
        AppointmentResponse creada = service.crear(req);
        var ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** PUT /appointments/{id} -> actualiza (también cambia el estado); 409 si ya fue atendida. */
    @PutMapping("/{id}")
    public AppointmentResponse actualizar(@PathVariable Long id, @Valid @RequestBody AppointmentRequest req) {
        return service.actualizar(id, req);
    }

    /** DELETE /appointments/{id} -> 204; 409 si ya fue atendida. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
