package com.clinica.owner;

import com.clinica.owner.Dtos.OwnerRequest;
import com.clinica.owner.Dtos.OwnerResponse;
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

/**
 * API REST de propietarios (ruta base /owners). El controlador solo recibe la petición,
 * la valida (@Valid) y delega todo el trabajo en OwnerService.
 */
@RestController
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService service;

    public OwnerController(OwnerService service) {
        this.service = service;
    }

    /** GET /owners -> lista todos (200). */
    @GetMapping
    public List<OwnerResponse> listar() {
        return service.listar();
    }

    /** GET /owners/{id} -> uno solo (200) o 404. */
    @GetMapping("/{id}")
    public OwnerResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** POST /owners -> registra (201 Created con cabecera Location) o 400 / 409. */
    @PostMapping
    public ResponseEntity<OwnerResponse> crear(@Valid @RequestBody OwnerRequest req) {
        OwnerResponse creado = service.crear(req);
        var ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** PUT /owners/{id} -> actualiza (200) o 400 / 404 / 409. */
    @PutMapping("/{id}")
    public OwnerResponse actualizar(@PathVariable Long id, @Valid @RequestBody OwnerRequest req) {
        return service.actualizar(id, req);
    }

    /** DELETE /owners/{id} -> 204 si se elimina; 409 si tiene mascotas; 503 si Pet-Service no responde. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
