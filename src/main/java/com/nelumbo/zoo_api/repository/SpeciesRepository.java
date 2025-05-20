package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.models.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, Long> {
    Optional<Species> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT COUNT(a) > 0 FROM Animal a WHERE a.species.id = :speciesId")
    boolean existsAnimalsBySpeciesId(Long speciesId);
}