package com.clinica.appointment;

import com.clinica.appointment.Dtos.OwnerDto;
import com.clinica.appointment.Dtos.PetDetalleDto;
import com.clinica.appointment.Dtos.PetDto;
import com.clinica.appointment.Errores.NoEncontrado;
import com.clinica.appointment.Errores.ServicioNoDisponible;
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
        "spring.datasource.url=jdbc:h2:mem:appointmentsdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AppointmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository repository;

    @MockBean
    private PetClient petClient;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private PetDto mascota() {
        return new PetDto(1L, "Firulais", "Perro", "Labrador", "MACHO", LocalDate.of(2020, 1, 1),
                20.0, "Negro", 5L, "ACTIVO");
    }

    private String json(long petId, LocalDate fecha, String hora) {
        return """
                {"petId":%d,"fecha":"%s","hora":"%s","motivoConsulta":"Vacunación","observaciones":"Ninguna"}
                """.formatted(petId, fecha, hora);
    }

    // Caso 3: Programar cita -> mascota validada y cita creada
    @Test
    void postAppointments_conMascotaValida_devuelve201() throws Exception {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(1, LocalDate.now().plusDays(3), "10:30")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    // Caso 4: Programar cita para mascota inexistente -> HTTP 404 NOT FOUND
    @Test
    void postAppointments_mascotaInexistente_devuelve404() throws Exception {
        when(petClient.obtenerMascota(99L)).thenThrow(new NoEncontrado("La mascota con id 99 no existe"));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(99, LocalDate.now().plusDays(3), "10:30")))
                .andExpect(status().isNotFound());
    }

    @Test
    void postAppointments_fechaPasada_devuelve400() throws Exception {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(1, LocalDate.now().minusDays(2), "10:30")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postAppointments_mismaFechaYHora_devuelve409() throws Exception {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());
        String cuerpo = json(1, LocalDate.now().plusDays(5), "09:00");

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void postAppointments_petServiceCaido_devuelve503() throws Exception {
        when(petClient.obtenerMascota(1L)).thenThrow(new ServicioNoDisponible("Pet-Service no disponible"));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(1, LocalDate.now().plusDays(3), "10:30")))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getCitaCompleta_devuelveCitaMascotaYPropietario() throws Exception {
        when(petClient.obtenerMascota(1L)).thenReturn(mascota());
        when(petClient.obtenerDetalle(1L)).thenReturn(new PetDetalleDto(mascota(),
                new OwnerDto(5L, "Ana", "Pérez", "1001", "3001234567", "ana@mail.com", "Calle 1", LocalDateTime.now())));

        String respuesta = mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(1, LocalDate.now().plusDays(4), "11:00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(get("/appointments/" + id + "/completa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cita.id").value(id))
                .andExpect(jsonPath("$.mascota.nombre").value("Firulais"))
                .andExpect(jsonPath("$.propietario.nombre").value("Ana"));
    }

    @Test
    void getCitaInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/appointments/9999")).andExpect(status().isNotFound());
    }
}
