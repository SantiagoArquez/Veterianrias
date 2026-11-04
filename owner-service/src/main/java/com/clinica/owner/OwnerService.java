package com.clinica.owner;

import com.clinica.owner.Dtos.OwnerRequest;
import com.clinica.owner.Dtos.OwnerResponse;
import com.clinica.owner.Errores.Conflicto;
import com.clinica.owner.Errores.NoEncontrado;
import com.clinica.owner.Errores.ReglaNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de propietarios. Aquí viven las reglas:
 *  - Documento y correo no pueden repetirse.
 *  - El teléfono es obligatorio.
 *  - No se puede eliminar un propietario que tenga mascotas.
 */
@Service
public class OwnerService {

    private final OwnerRepository repository;
    private final PetClient petClient;

    public OwnerService(OwnerRepository repository, PetClient petClient) {
        this.repository = repository;
        this.petClient = petClient;
    }

    @Transactional(readOnly = true)
    public List<OwnerResponse> listar() {
        return repository.findAll().stream().map(OwnerResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public OwnerResponse obtener(Long id) {
        return OwnerResponse.desde(buscar(id));
    }

    @Transactional
    public OwnerResponse crear(OwnerRequest req) {
        validarTelefono(req);
        if (repository.existsByDocumento(req.documento())) {
            throw new Conflicto("Ya existe un propietario con el documento " + req.documento());
        }
        if (repository.existsByCorreo(req.correo())) {
            throw new Conflicto("Ya existe un propietario con el correo " + req.correo());
        }
        Owner owner = new Owner();
        copiar(req, owner);
        return OwnerResponse.desde(repository.save(owner));
    }

    @Transactional
    public OwnerResponse actualizar(Long id, OwnerRequest req) {
        validarTelefono(req);
        Owner owner = buscar(id);
        // Al editar, el documento/correo solo choca si pertenece a OTRO propietario
        if (repository.existsByDocumentoAndIdNot(req.documento(), id)) {
            throw new Conflicto("Ya existe otro propietario con el documento " + req.documento());
        }
        if (repository.existsByCorreoAndIdNot(req.correo(), id)) {
            throw new Conflicto("Ya existe otro propietario con el correo " + req.correo());
        }
        copiar(req, owner);
        return OwnerResponse.desde(repository.save(owner));
    }

    @Transactional
    public void eliminar(Long id) {
        Owner owner = buscar(id);
        // Consulta a Pet-Service: si tiene mascotas asociadas no se puede borrar
        if (petClient.tieneMascotas(id)) {
            throw new Conflicto("No se puede eliminar el propietario " + id + " porque tiene mascotas asociadas");
        }
        repository.delete(owner);
    }

    /** Busca por id o lanza 404. */
    private Owner buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoEncontrado("Propietario con id " + id + " no encontrado"));
    }

    /** Regla de negocio: se valida también aquí (no solo en el DTO) para que sea testeable. */
    private void validarTelefono(OwnerRequest req) {
        if (req.telefono() == null || req.telefono().isBlank()) {
            throw new ReglaNegocio("Todos los propietarios deben tener un teléfono de contacto");
        }
    }

    /** Pasa los datos del DTO a la entidad, quitando espacios sobrantes. */
    private void copiar(OwnerRequest req, Owner owner) {
        owner.setNombre(req.nombre().trim());
        owner.setApellido(req.apellido().trim());
        owner.setDocumento(req.documento().trim());
        owner.setTelefono(req.telefono().trim());
        owner.setCorreo(req.correo().trim());
        owner.setDireccion(req.direccion());
    }
}
