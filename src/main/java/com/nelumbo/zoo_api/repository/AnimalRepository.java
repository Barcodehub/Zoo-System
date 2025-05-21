package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findBySpecies(Species species);
    List<Animal> findByZone(Zone zone);
    boolean existsBySpecies(Species species);
    boolean existsByZone(Zone zone);

    @Query("SELECT a FROM Animal a WHERE DATE(a.registrationDate) = DATE(:date)")
    List<Animal> findByRegistrationDate(@Param("date") Date date);

    @Query("SELECT a FROM Animal a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Animal> searchAnimals(@Param("query") String query);

    @Query("SELECT a FROM Animal a WHERE a.zone.id = :zoneId")
    List<Animal> findByZoneId(@Param("zoneId") Long zoneId);

    @Query("SELECT a FROM Animal a WHERE a.species.id = :speciesId")
    List<Animal> findBySpeciesId(@Param("speciesId") Long speciesId);

    @Query("SELECT a FROM Animal a WHERE a.registrationDate BETWEEN :startDate AND :endDate")
    List<Animal> findByRegistrationDateBetween(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    long countByZone(Zone zone);
    long countBySpecies(Species species);
    List<Animal> findByRegistrationDate(LocalDate date);

    List<Animal> findByNameContainingIgnoreCase(String name);
}