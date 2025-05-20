package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT COUNT(a) > 0 FROM Animal a WHERE a.zone.id = :zoneId")
    boolean existsAnimalsByZoneId(Long zoneId);

    List<Zone> findByNameContainingIgnoreCase(String name);

}