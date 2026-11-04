package com.clinica.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** DTOs: los "formularios" JSON de entrada/salida de la API (todos juntos en un archivo). */
public class Dtos {

    private Dtos() {
    }

    /** Datos que envía el cliente para programar o modificar una cita. */
    public record AppointmentRequest(
            @NotNull(message = "La mascota (petId) es obligatoria")
            Long petId,

            @NotNull(message = "La fecha es obligatoria (formato yyyy-MM-dd)")
            LocalDate fecha,

            @NotNull(message = "La hora es obligatoria (formato HH:mm)")
            LocalTime hora,

            @NotBlank(message = "El motivo de consulta es obligatorio")
            @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
            String motivoConsulta,

            @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
            String observaciones,

            /** Solo se usa al actualizar; al crear, la cita siempre queda PROGRAMADA. */
            Appointment.Estado estado) {
    }

    /** Datos que devuelve la API de una cita. */
    public record AppointmentResponse(Long id, Long petId, LocalDate fecha, LocalTime hora,
                                      String motivoConsulta, String observaciones, Appointment.Estado estado) {

        public static AppointmentResponse desde(Appointment a) {
            return new AppointmentResponse(a.getId(), a.getPetId(), a.getFecha(), a.getHora(),
                    a.getMotivoConsulta(), a.getObservaciones(), a.getEstado());
        }
    }

    /** Mascota tal como la devuelve Pet-Service (DTO de integración). */
    public record PetDto(Long id, String nombre, String especie, String raza, String sexo,
                         LocalDate fechaNacimiento, Double peso, String color, Long ownerId, String estado) {
    }

    /** Propietario tal como llega desde Owner-Service, a través de Pet-Service (DTO de integración). */
    public record OwnerDto(Long id, String nombre, String apellido, String documento, String telefono,
                           String correo, String direccion, LocalDateTime fechaRegistro) {
    }

    /** Respuesta de GET /pets/{id}/detalle de Pet-Service: mascota + propietario. */
    public record PetDetalleDto(PetDto mascota, OwnerDto propietario) {
    }

    /** Respuesta compuesta de esta API: cita + mascota + propietario. */
    public record CitaCompletaResponse(AppointmentResponse cita, PetDto mascota, OwnerDto propietario) {
    }

    /** Formato uniforme de los errores. */
    public record ApiError(LocalDateTime timestamp, int status, String error, String mensaje, String ruta) {
    }
}
