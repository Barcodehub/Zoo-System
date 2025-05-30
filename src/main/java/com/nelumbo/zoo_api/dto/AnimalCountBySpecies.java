package com.nelumbo.zoo_api.dto;

public record AnimalCountBySpecies(String speciesName, Long animalCount) {

    public Long getCount() {
        return this.animalCount;
    }
}