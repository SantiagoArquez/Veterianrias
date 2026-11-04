package com.clinica.appointment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

/** Entidad CITA: cada objeto es una fila de la tabla "appointments". */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_pet", columnList = "pet_id"),
        @Index(name = "idx_appointment_fecha_hora", columnList = "fecha, hora")
})
public class Appointment {

    /** Estado de la cita: Programada, Confirmada, Atendida o Cancelada. */
    public enum Estado { PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia LÓGICA a la mascota (vive en Pet-Service; aquí solo se guarda su id). */
    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    @Column(length = 1000)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    // ---- Getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
