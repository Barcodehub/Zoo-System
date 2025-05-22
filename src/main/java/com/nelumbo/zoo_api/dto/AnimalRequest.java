package com.nelumbo.zoo_api.dto;

import jakarta.validation.constraints.NotBlank;


public record AnimalRequest(
        @NotBlank
        String name,
        //Hay casos donde se eliminan si no tiene especie o zona (entonces pueden ser nulos)

        Long speciesId,

        Long zoneId)
{}
