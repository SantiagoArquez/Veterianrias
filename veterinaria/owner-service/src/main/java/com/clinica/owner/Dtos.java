package com.clinica.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTOs (objetos de transferencia): son los "formularios" JSON que entran y salen de la API.
 * Se usan en lugar de exponer la entidad directamente.
 */
public class Dtos {

    private Dtos() {
    }

    /** Datos que envía el cliente para crear o actualizar un propietario (con sus validaciones). */
    public record OwnerRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
            String nombre,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 80, message = "El apellido no puede superar 80 caracteres")
            String apellido,

            @NotBlank(message = "El documento es obligatorio")
            @Size(max = 30, message = "El documento no puede superar 30 caracteres")
            String documento,

            @NotBlank(message = "El teléfono de contacto es obligatorio")
            @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
            String telefono,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 120, message = "El correo no puede superar 120 caracteres")
            String correo,

            @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
            String direccion) {
    }

    /** Datos que devuelve la API de un propietario. */
    public record OwnerResponse(Long id, String nombre, String apellido, String documento,
                                String telefono, String correo, String direccion,
                                LocalDateTime fechaRegistro) {

        /** Convierte la entidad guardada en la base de datos en la respuesta JSON. */
        public static OwnerResponse desde(Owner o) {
            return new OwnerResponse(o.getId(), o.getNombre(), o.getApellido(), o.getDocumento(),
                    o.getTelefono(), o.getCorreo(), o.getDireccion(), o.getFechaRegistro());
        }
    }

    /** Formato uniforme de todos los errores que devuelve el servicio. */
    public record ApiError(LocalDateTime timestamp, int status, String error, String mensaje, String ruta) {
    }
}
