package com.clinica.pet;

import com.clinica.pet.Dtos.PetDetalleResponse;
import com.clinica.pet.Dtos.PetRequest;
import com.clinica.pet.Dtos.PetResponse;
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

/** API REST de mascotas (ruta base /pets). Solo recibe, valida y delega en PetService. */
@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    /** GET /pets -> todas las mascotas. */
    @GetMapping
    public List<PetResponse> listar() {
        return service.listar();
    }

    /** GET /pets/{id} -> una mascota (200) o 404. */
    @GetMapping("/{id}")
    public PetResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** GET /pets/owner/{ownerId} -> mascotas de un propietario (lo usa Owner-Service). */
    @GetMapping("/owner/{ownerId}")
    public List<PetResponse> listarPorPropietario(@PathVariable Long ownerId) {
        return service.listarPorPropietario(ownerId);
    }

    /** GET /pets/{id}/detalle -> mascota + propietario (lo usa Appointment-Service). */
    @GetMapping("/{id}/detalle")
    public PetDetalleResponse detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    /** POST /pets -> 201; 400 si el propietario no existe o hay datos inválidos; 503 si Owner-Service cae. */
    @PostMapping
    public ResponseEntity<PetResponse> crear(@Valid @RequestBody PetRequest req) {
        PetResponse creada = service.crear(req);
        var ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** PUT /pets/{id} -> actualiza (también sirve para cambiar el estado). */
    @PutMapping("/{id}")
    public PetResponse actualizar(@PathVariable Long id, @Valid @RequestBody PetRequest req) {
        return service.actualizar(id, req);
    }

    /** DELETE /pets/{id} -> 204 o 404. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
