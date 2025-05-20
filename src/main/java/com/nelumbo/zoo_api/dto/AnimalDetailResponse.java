package com.nelumbo.zoo_api.dto;

public record AnimalDetailResponse(
        Long id,
        String name,
        String species,
        String zone,
        String registrationDate
) {}