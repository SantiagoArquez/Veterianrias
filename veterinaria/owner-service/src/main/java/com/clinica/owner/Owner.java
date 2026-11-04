package com.clinica.owner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Entidad PROPIETARIO: cada objeto de esta clase es una fila de la tabla "owners".
 * El documento y el correo son únicos (la base de datos también lo garantiza).
 */
@Entity
@Table(name = "owners", uniqueConstraints = {
        @UniqueConstraint(name = "uk_owner_documento", columnNames = "documento"),
        @UniqueConstraint(name = "uk_owner_correo", columnNames = "correo")
})
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id autoincremental
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(nullable = false, length = 30)
    private String documento;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(length = 200)
    private String direccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Se ejecuta justo antes de guardar por primera vez: pone la fecha de registro automáticamente. */
    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    // ---- Getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
