package com.clinica.owner;

import com.clinica.owner.Errores.ServicioNoDisponible;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBAS DE INTEGRACIÓN: levantan Spring completo (controlador + servicio + JPA) con una base
 * H2 en memoria en vez de MySQL. Solo se simula (@MockBean) la llamada a Pet-Service.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ownersdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class OwnerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OwnerRepository repository;

    @MockBean
    private PetClient petClient;

    @BeforeEach
    void limpiar() {
        repository.deleteAll(); // cada prueba empieza con la tabla vacía
    }

    private String json(String documento, String correo, String telefono) {
        return """
                {"nombre":"Ana","apellido":"Pérez","documento":"%s","telefono":%s,
                 "correo":"%s","direccion":"Calle 1"}
                """.formatted(documento, telefono == null ? "null" : "\"" + telefono + "\"", correo);
    }

    /** Crea un propietario por HTTP y devuelve su id. */
    private long crearPropietario(String documento, String correo) throws Exception {
        String body = mockMvc.perform(post("/owners").contentType(MediaType.APPLICATION_JSON)
                        .content(json(documento, correo, "3001112222")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    // Caso 1 del enunciado: registrar propietario -> HTTP 201
    @Test
    void postOwners_devuelve201() throws Exception {
        mockMvc.perform(post("/owners").contentType(MediaType.APPLICATION_JSON)
                        .content(json("1001", "ana@mail.com", "3001112222")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.documento").value("1001"))
                .andExpect(jsonPath("$.fechaRegistro").exists());
    }

    @Test
    void postOwners_documentoDuplicado_devuelve409() throws Exception {
        crearPropietario("1001", "ana@mail.com");

        mockMvc.perform(post("/owners").contentType(MediaType.APPLICATION_JSON)
                        .content(json("1001", "otro@mail.com", "3001112222")))
                .andExpect(status().isConflict());
    }

    @Test
    void postOwners_correoDuplicado_devuelve409() throws Exception {
        crearPropietario("1001", "ana@mail.com");

        mockMvc.perform(post("/owners").contentType(MediaType.APPLICATION_JSON)
                        .content(json("2002", "ana@mail.com", "3001112222")))
                .andExpect(status().isConflict());
    }

    @Test
    void postOwners_sinTelefono_devuelve400() throws Exception {
        mockMvc.perform(post("/owners").contentType(MediaType.APPLICATION_JSON)
                        .content(json("1001", "ana@mail.com", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/owners/9999")).andExpect(status().isNotFound());
    }

    @Test
    void deleteOwner_sinMascotas_devuelve204() throws Exception {
        long id = crearPropietario("1001", "ana@mail.com");
        when(petClient.tieneMascotas(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/owners/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/owners/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deleteOwner_conMascotas_devuelve409() throws Exception {
        long id = crearPropietario("1001", "ana@mail.com");
        when(petClient.tieneMascotas(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/owners/" + id)).andExpect(status().isConflict());
    }

    @Test
    void deleteOwner_petServiceCaido_devuelve503() throws Exception {
        long id = crearPropietario("1001", "ana@mail.com");
        when(petClient.tieneMascotas(anyLong())).thenThrow(new ServicioNoDisponible("Pet-Service no disponible"));

        mockMvc.perform(delete("/owners/" + id)).andExpect(status().isServiceUnavailable());
    }
}
