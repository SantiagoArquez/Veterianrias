package com.clinica.pet;

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

/**
 * Entidad MASCOTA: cada objeto es una fila de la tabla "pets".
 * Incluye los dos enumerados que usa (sexo y estado) para no crear más archivos.
 */
@Entity
@Table(name = "pets", indexes = @Index(name = "idx_pet_owner", columnList = "owner_id"))
public class Pet {

    /** Sexo de la mascota. */
    public enum Sexo { MACHO, HEMBRA }

    /** Estado clínico: Activo, En Tratamiento, Recuperado o Fallecido. */
    public enum Estado { ACTIVO, EN_TRATAMIENTO, RECUPERADO, FALLECIDO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String especie;

    @Column(nullable = false, length = 80)
    private String raza;

    @Enumerated(EnumType.STRING) // se guarda como texto ("MACHO"), no como número
    @Column(nullable = false, length = 10)
    private Sexo sexo;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private Double peso;

    @Column(length = 60)
    private String color;

    /**
     * Referencia LÓGICA al propietario. El propietario vive en otro microservicio y otra base de
     * datos, por eso aquí solo se guarda su id (no hay clave foránea).
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    // ---- Getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
