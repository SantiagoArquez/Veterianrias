package com.clinica.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTOs: los "formularios" JSON de entrada/salida de la API (todos juntos en un archivo). */
public class Dtos {

    private Dtos() {
    }

    /** Datos que envía el cliente para crear o actualizar una mascota. */
    public record PetRequest(
            @NotBlank(message = "El nombre de la mascota es obligatorio")
            @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
            String nombre,

            @NotBlank(message = "La especie es obligatoria")
            @Size(max = 60, message = "La especie no puede superar 60 caracteres")
            String especie,

            @NotBlank(message = "La raza es obligatoria")
            @Size(max = 80, message = "La raza no puede superar 80 caracteres")
            String raza,

            @NotNull(message = "El sexo es obligatorio (MACHO o HEMBRA)")
            Pet.Sexo sexo,

            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
            LocalDate fechaNacimiento,

            @NotNull(message = "El peso es obligatorio")
            @Positive(message = "El peso debe ser mayor que cero")
            Double peso,

            @Size(max = 60, message = "El color no puede superar 60 caracteres")
            String color,

            @NotNull(message = "El propietario (ownerId) es obligatorio")
            Long ownerId,

            /** Opcional: si no se envía, la mascota se registra como ACTIVO. */
            Pet.Estado estado) {
    }

    /** Datos que devuelve la API de una mascota. */
    public record PetResponse(Long id, String nombre, String especie, String raza, Pet.Sexo sexo,
                              LocalDate fechaNacimiento, Double peso, String color, Long ownerId,
                              Pet.Estado estado) {

        public static PetResponse desde(Pet p) {
            return new PetResponse(p.getId(), p.getNombre(), p.getEspecie(), p.getRaza(), p.getSexo(),
                    p.getFechaNacimiento(), p.getPeso(), p.getColor(), p.getOwnerId(), p.getEstado());
        }
    }

    /** Propietario tal como lo devuelve Owner-Service (DTO de integración entre servicios). */
    public record OwnerDto(Long id, String nombre, String apellido, String documento, String telefono,
                           String correo, String direccion, LocalDateTime fechaRegistro) {
    }

    /** Respuesta compuesta: la mascota junto con los datos de su propietario. */
    public record PetDetalleResponse(PetResponse mascota, OwnerDto propietario) {
    }

    /** Formato uniforme de los errores. */
    public record ApiError(LocalDateTime timestamp, int status, String error, String mensaje, String ruta) {
    }
}
