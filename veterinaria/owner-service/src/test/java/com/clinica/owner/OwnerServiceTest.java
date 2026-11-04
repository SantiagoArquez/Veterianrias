package com.clinica.owner;

import com.clinica.owner.Dtos.OwnerRequest;
import com.clinica.owner.Dtos.OwnerResponse;
import com.clinica.owner.Errores.Conflicto;
import com.clinica.owner.Errores.NoEncontrado;
import com.clinica.owner.Errores.ReglaNegocio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS UNITARIAS de OwnerService. Se simulan (mock) el repositorio y el cliente de Pet-Service,
 * así que no hay base de datos ni red: solo se prueba la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository repository;

    @Mock
    private PetClient petClient;

    @InjectMocks
    private OwnerService service;

    private OwnerRequest request() {
        return new OwnerRequest("Ana", "Pérez", "1001", "3001234567", "ana@mail.com", "Calle 1 # 2-3");
    }

    private Owner existente(Long id) {
        Owner o = new Owner();
        o.setId(id);
        o.setNombre("Ana");
        o.setApellido("Pérez");
        o.setDocumento("1001");
        o.setTelefono("3001234567");
        o.setCorreo("ana@mail.com");
        return o;
    }

    @Test
    void crear_guardaPropietario() {
        when(repository.existsByDocumento("1001")).thenReturn(false);
        when(repository.existsByCorreo("ana@mail.com")).thenReturn(false);
        when(repository.save(any(Owner.class))).thenAnswer(inv -> {
            Owner o = inv.getArgument(0);
            o.setId(1L); // simula el id que asignaría la base de datos
            return o;
        });

        OwnerResponse r = service.crear(request());

        assertEquals(1L, r.id());
        assertEquals("Ana", r.nombre());
    }

    @Test
    void crear_documentoDuplicado_lanzaConflicto() {
        when(repository.existsByDocumento("1001")).thenReturn(true);

        assertThrows(Conflicto.class, () -> service.crear(request()));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_correoDuplicado_lanzaConflicto() {
        when(repository.existsByDocumento("1001")).thenReturn(false);
        when(repository.existsByCorreo("ana@mail.com")).thenReturn(true);

        assertThrows(Conflicto.class, () -> service.crear(request()));
    }

    @Test
    void crear_sinTelefono_lanzaReglaDeNegocio() {
        OwnerRequest sinTelefono = new OwnerRequest("Ana", "Pérez", "1001", " ", "ana@mail.com", null);

        assertThrows(ReglaNegocio.class, () -> service.crear(sinTelefono));
    }

    @Test
    void obtener_existente_devuelvePropietario() {
        when(repository.findById(5L)).thenReturn(Optional.of(existente(5L)));

        assertEquals(5L, service.obtener(5L).id());
    }

    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoEncontrado.class, () -> service.obtener(99L));
    }

    @Test
    void actualizar_modificaDatos() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente(1L)));
        when(repository.existsByDocumentoAndIdNot("2002", 1L)).thenReturn(false);
        when(repository.existsByCorreoAndIdNot("nuevo@mail.com", 1L)).thenReturn(false);
        when(repository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        OwnerRequest nuevo = new OwnerRequest("Ana María", "Pérez", "2002", "3009999999", "nuevo@mail.com", "Cra 5");
        OwnerResponse r = service.actualizar(1L, nuevo);

        assertEquals("Ana María", r.nombre());
        assertEquals("2002", r.documento());
    }

    @Test
    void actualizar_documentoDeOtroPropietario_lanzaConflicto() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente(1L)));
        when(repository.existsByDocumentoAndIdNot("1001", 1L)).thenReturn(true);

        assertThrows(Conflicto.class, () -> service.actualizar(1L, request()));
    }

    @Test
    void eliminar_sinMascotas_elimina() {
        Owner o = existente(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(o));
        when(petClient.tieneMascotas(1L)).thenReturn(false);

        service.eliminar(1L);

        verify(repository).delete(o);
    }

    @Test
    void eliminar_conMascotas_lanzaConflictoYNoElimina() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente(1L)));
        when(petClient.tieneMascotas(1L)).thenReturn(true);

        assertThrows(Conflicto.class, () -> service.eliminar(1L));
        verify(repository, never()).delete(any(Owner.class));
    }
}
