package com.nelumbo.zoo_api.dto;

public record AnimalCountByZone(String zoneName, Long animalCount) {


    public Long getCount() {
        return this.animalCount;
    }

}

