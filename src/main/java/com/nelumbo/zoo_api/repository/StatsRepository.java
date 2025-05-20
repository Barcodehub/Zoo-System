package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.dto.AnimalCountBySpecies;
import com.nelumbo.zoo_api.dto.AnimalCountByZone;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepository {
    @Query("SELECT new com.zoo.dto.AnimalCountByZone(z.name, COUNT(a)) " +
            "FROM Animal a JOIN a.zone z GROUP BY z.name")
    List<AnimalCountByZone> countAnimalsByZone();

    @Query("SELECT new com.zoo.dto.AnimalCountBySpecies(s.name, COUNT(a)) " +
            "FROM Animal a JOIN a.species s GROUP BY s.name")
    List<AnimalCountBySpecies> countAnimalsBySpecies();

    @Query("SELECT (COUNT(c) * 100.0 / (SELECT COUNT(c2) FROM Comment c2 WHERE c2.parentComment IS NULL)) " +
            "FROM Comment c WHERE c.parentComment IS NOT NULL")
    Double calculateAnsweredCommentsPercentage();
}