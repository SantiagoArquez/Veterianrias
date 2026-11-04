package com.clinica.pet;

import com.clinica.pet.Dtos.OwnerDto;
import com.clinica.pet.Dtos.PetDetalleResponse;
import com.clinica.pet.Dtos.PetRequest;
import com.clinica.pet.Dtos.PetResponse;
import com.clinica.pet.Errores.NoEncontrado;
import com.clinica.pet.Errores.ReglaNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de mascotas. Reglas:
 *  - Toda mascota pertenece a un propietario que EXISTA (se consulta a Owner-Service).
 *  - No se permiten mascotas sin nombre, sin especie/raza ni con fecha de nacimiento futura.
 */
@Service
public class PetService {

    private final PetRepository repository;
    private final OwnerClient ownerClient;

    public PetService(PetRepository repository, OwnerClient ownerClient) {
        this.repository = repository;
        this.ownerClient = ownerClient;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listar() {
        return repository.findAll().stream().map(PetResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse obtener(Long id) {
        return PetResponse.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listarPorPropietario(Long ownerId) {
        return repository.findByOwnerId(ownerId).stream().map(PetResponse::desde).toList();
    }

    /**
     * Mascota + propietario. Pet-Service le pregunta a Owner-Service por el dueño, así se forma la
     * cadena Appointment -> Pet -> Owner.
     */
    @Transactional(readOnly = true)
    public PetDetalleResponse detalle(Long id) {
        Pet pet = buscar(id);
        OwnerDto propietario = ownerClient.obtenerPropietario(pet.getOwnerId());
        return new PetDetalleResponse(PetResponse.desde(pet), propietario);
    }

    @Transactional
    public PetResponse crear(PetRequest req) {
        validarDatos(req);
        validarPropietario(req.ownerId());
        Pet pet = new Pet();
        copiar(req, pet);
        // Si no se indica estado, la mascota nace ACTIVA
        pet.setEstado(req.estado() != null ? req.estado() : Pet.Estado.ACTIVO);
        return PetResponse.desde(repository.save(pet));
    }

    @Transactional
    public PetResponse actualizar(Long id, PetRequest req) {
        validarDatos(req);
        Pet pet = buscar(id);
        // Solo se vuelve a consultar a Owner-Service si cambió de dueño
        if (!pet.getOwnerId().equals(req.ownerId())) {
            validarPropietario(req.ownerId());
        }
        copiar(req, pet);
        if (req.estado() != null) {
            pet.setEstado(req.estado());
        }
        return PetResponse.desde(repository.save(pet));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    /** Busca por id o lanza 404. */
    private Pet buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoEncontrado("Mascota con id " + id + " no encontrada"));
    }

    /** Reglas de datos (también validadas aquí para poder probarlas sin la capa web). */
    private void validarDatos(PetRequest req) {
        if (req.nombre() == null || req.nombre().isBlank()) {
            throw new ReglaNegocio("No se permiten mascotas sin nombre");
        }
        if (req.especie() == null || req.especie().isBlank() || req.raza() == null || req.raza().isBlank()) {
            throw new ReglaNegocio("La especie y la raza de la mascota son obligatorias");
        }
        if (req.fechaNacimiento() != null && req.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new ReglaNegocio("La fecha de nacimiento no puede ser futura");
        }
    }

    /**
     * Regla: el propietario debe existir. Si Owner-Service dice 404 lo convertimos en 400 (Bad Request);
     * si Owner-Service está caído, la excepción ServicioNoDisponible sube tal cual (503).
     */
    private void validarPropietario(Long ownerId) {
        try {
            ownerClient.obtenerPropietario(ownerId);
        } catch (NoEncontrado e) {
            throw new ReglaNegocio("El propietario con id " + ownerId + " no existe");
        }
    }

    private void copiar(PetRequest req, Pet pet) {
        pet.setNombre(req.nombre().trim());
        pet.setEspecie(req.especie().trim());
        pet.setRaza(req.raza().trim());
        pet.setSexo(req.sexo());
        pet.setFechaNacimiento(req.fechaNacimiento());
        pet.setPeso(req.peso());
        pet.setColor(req.color());
        pet.setOwnerId(req.ownerId());
    }
}
