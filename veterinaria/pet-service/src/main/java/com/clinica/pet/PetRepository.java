package com.clinica.pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "pets" (Spring Data genera el SQL a partir del nombre de los métodos). */
public interface PetRepository extends JpaRepository<Pet, Long> {

    /** Todas las mascotas de un propietario. */
    List<Pet> findByOwnerId(Long ownerId);
}
