package com.clinica.owner;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla "owners". Spring Data genera la implementación automáticamente
 * a partir de los nombres de los métodos (no hay que escribir SQL).
 */
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByCorreo(String correo);

    /** Variantes para actualizar: ignoran al propietario que se está editando. */
    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByCorreoAndIdNot(String correo, Long id);
}
