package com.clinica.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Acceso a la tabla "appointments" (Spring Data genera el SQL a partir del nombre de los métodos). */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Todas las citas de una mascota. */
    List<Appointment> findByPetId(Long petId);

    /**
     * ¿Hay otra cita en esa fecha y hora que NO tenga el estado indicado? Se usa con CANCELADA:
     * una cita cancelada libera el horario, por eso no cuenta como duplicada.
     */
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, Appointment.Estado estado);

    /** Igual que la anterior pero ignorando la cita que se está editando (para reprogramar). */
    boolean existsByFechaAndHoraAndEstadoNotAndIdNot(LocalDate fecha, LocalTime hora,
                                                     Appointment.Estado estado, Long id);
}
