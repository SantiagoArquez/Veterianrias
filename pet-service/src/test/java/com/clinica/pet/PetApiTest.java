package com.clinica.pet;

import com.clinica.pet.Dtos.OwnerDto;
import com.clinica.pet.Errores.NoEncontrado;
import com.clinica.pet.Errores.ServicioNoDisponible;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBAS DE INTEGRACIÓN: levantan Spring completo con una base H2 en memoria en lugar de MySQL.
 * Las llamadas a otros microservicios se simulan con @MockBean (no hace falta red ni Docker).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:petsdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class PetApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository repository;

    @MockBean
    private OwnerClient ownerClient;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String json(String nombre, String fechaNacimiento, long ownerId) {
        return """
                {"nombre":"%s","especie":"Perro","raza":"Labrador","sexo":"MACHO",
                 "fechaNacimiento":"%s","peso":25.5,"color":"Negro","ownerId":%d}
                """.formatted(nombre, fechaNacimiento, ownerId);
    }

    private OwnerDto propietario(long id) {
        return new OwnerDto(id, "Ana", "Pérez", "1001", "3001234567", "ana@mail.com", "Calle 1", LocalDateTime.now());
    }

    // Caso 2: Registrar mascota asociada -> propietario validado y mascota registrada
    @Test
    void postPets_conPropietarioValido_devuelve201() throws Exception {
        when(ownerClient.obtenerPropietario(1L)).thenReturn(propietario(1L));

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Firulais", "2020-05-01", 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    // Caso 5: Crear mascota para propietario inexistente -> HTTP 400 BAD REQUEST
    @Test
    void postPets_conPropietarioInexistente_devuelve400() throws Exception {
        when(ownerClient.obtenerPropietario(99L)).thenThrow(new NoEncontrado("no existe"));

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Firulais", "2020-05-01", 99)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postPets_ownerServiceCaido_devuelve503() throws Exception {
        when(ownerClient.obtenerPropietario(1L)).thenThrow(new ServicioNoDisponible("Owner-Service no disponible"));

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Firulais", "2020-05-01", 1)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void postPets_sinNombre_devuelve400() throws Exception {
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "2020-05-01", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postPets_fechaNacimientoFutura_devuelve400() throws Exception {
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Firulais", LocalDate.now().plusDays(10).toString(), 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPetsPorPropietario_devuelveSoloSusMascotas() throws Exception {
        when(ownerClient.obtenerPropietario(1L)).thenReturn(propietario(1L));
        when(ownerClient.obtenerPropietario(2L)).thenReturn(propietario(2L));
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON).content(json("Rex", "2019-01-01", 1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON).content(json("Luna", "2021-01-01", 2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/pets/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Rex"));
    }

    @Test
    void getPetInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/pets/9999")).andExpect(status().isNotFound());
    }
}
