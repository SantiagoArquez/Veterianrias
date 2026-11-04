package com.clinica.pet;

import com.clinica.pet.Dtos.OwnerDto;
import com.clinica.pet.Dtos.PetDetalleResponse;
import com.clinica.pet.Dtos.PetRequest;
import com.clinica.pet.Dtos.PetResponse;
import com.clinica.pet.Errores.NoEncontrado;
import com.clinica.pet.Errores.ReglaNegocio;
import com.clinica.pet.Errores.ServicioNoDisponible;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
class PetServiceTest {

    @Mock
    private PetRepository repository;

    @Mock
    private OwnerClient ownerClient;

    @InjectMocks
    private PetService service;

    private OwnerDto propietario(Long id) {
        return new OwnerDto(id, "Ana", "Pérez", "1001", "3001234567", "ana@mail.com", null, LocalDateTime.now());
    }

    private PetRequest request(String nombre, LocalDate nacimiento, Long ownerId) {
        return new PetRequest(nombre, "Perro", "Labrador", Pet.Sexo.MACHO, nacimiento, 25.5, "Negro", ownerId, null);
    }

    private Pet existente(Long id, Long ownerId) {
        Pet p = new Pet();
        p.setId(id);
        p.setNombre("Firulais");
        p.setEspecie("Perro");
        p.setRaza("Labrador");
        p.setSexo(Pet.Sexo.MACHO);
        p.setFechaNacimiento(LocalDate.of(2020, 1, 1));
        p.setPeso(20.0);
        p.setOwnerId(ownerId);
        p.setEstado(Pet.Estado.ACTIVO);
        return p;
    }

    // ----- Registro de mascotas y asociación con propietarios -----
    @Test
    void crear_conPropietarioValido_registraMascotaActiva() {
        when(ownerClient.obtenerPropietario(1L)).thenReturn(propietario(1L));
        when(repository.save(any(Pet.class))).thenAnswer(inv -> {
            Pet p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PetResponse r = service.crear(request("Firulais", LocalDate.of(2020, 5, 1), 1L));

        assertEquals(10L, r.id());
        assertEquals(1L, r.ownerId());
        assertEquals(Pet.Estado.ACTIVO, r.estado());
    }

    @Test
    void crear_conPropietarioInexistente_lanzaReglaDeNegocio() {
        when(ownerClient.obtenerPropietario(99L)).thenThrow(new NoEncontrado("no existe"));

        assertThrows(ReglaNegocio.class,
                () -> service.crear(request("Firulais", LocalDate.of(2020, 5, 1), 99L)));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_ownerServiceCaido_propagaServicioNoDisponible() {
        when(ownerClient.obtenerPropietario(1L)).thenThrow(new ServicioNoDisponible("caído"));

        assertThrows(ServicioNoDisponible.class,
                () -> service.crear(request("Firulais", LocalDate.of(2020, 5, 1), 1L)));
    }

    // ----- Validación de datos -----
    @Test
    void crear_sinNombre_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocio.class,
                () -> service.crear(request("  ", LocalDate.of(2020, 5, 1), 1L)));
    }

    @Test
    void crear_fechaNacimientoFutura_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocio.class,
                () -> service.crear(request("Firulais", LocalDate.now().plusDays(1), 1L)));
    }

    // ----- Consultas -----
    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> service.obtener(5L));
    }

    @Test
    void detalle_devuelveMascotaYPropietario() {
        when(repository.findById(3L)).thenReturn(Optional.of(existente(3L, 1L)));
        when(ownerClient.obtenerPropietario(1L)).thenReturn(propietario(1L));

        PetDetalleResponse d = service.detalle(3L);

        assertEquals("Firulais", d.mascota().nombre());
        assertEquals("Ana", d.propietario().nombre());
    }

    // ----- Actualización -----
    @Test
    void actualizar_cambioDeEstado_conservaPropietarioSinConsultarlo() {
        when(repository.findById(3L)).thenReturn(Optional.of(existente(3L, 1L)));
        when(repository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));
        PetRequest req = new PetRequest("Firulais", "Perro", "Labrador", Pet.Sexo.MACHO,
                LocalDate.of(2020, 1, 1), 21.0, "Negro", 1L, Pet.Estado.EN_TRATAMIENTO);

        PetResponse r = service.actualizar(3L, req);

        assertEquals(Pet.Estado.EN_TRATAMIENTO, r.estado());
        verify(ownerClient, never()).obtenerPropietario(any());
    }
}
