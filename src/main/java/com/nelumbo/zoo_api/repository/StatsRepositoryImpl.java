package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.dto.AnimalCountBySpecies;
import com.nelumbo.zoo_api.dto.AnimalCountByZone;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class StatsRepositoryImpl implements StatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AnimalCountByZone> countAnimalsByZone() {
        return entityManager.createQuery(
                        "SELECT new com.zoo.dto.AnimalCountByZone(z.name, COUNT(a)) " +
                                "FROM Animal a JOIN a.zone z GROUP BY z.name",
                        AnimalCountByZone.class)
                .getResultList();
    }

    @Override
    public List<AnimalCountBySpecies> countAnimalsBySpecies() {
        return entityManager.createQuery(
                        "SELECT new com.zoo.dto.AnimalCountBySpecies(s.name, COUNT(a)) " +
                                "FROM Animal a JOIN a.species s GROUP BY s.name",
                        AnimalCountBySpecies.class)
                .getResultList();
    }

    @Override
    public Double calculateAnsweredCommentsPercentage() {
        return (Double) entityManager.createQuery(
                        "SELECT (COUNT(c) * 100.0 / (SELECT COUNT(c2) FROM Comment c2 WHERE c2.parentComment IS NULL)) " +
                                "FROM Comment c WHERE c.parentComment IS NOT NULL")
                .getSingleResult();
    }
}