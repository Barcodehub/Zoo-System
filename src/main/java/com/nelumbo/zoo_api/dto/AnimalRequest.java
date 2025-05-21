package com.nelumbo.zoo_api.dto;

import com.nelumbo.zoo_api.validation.annotations.SpeciesExists;
import com.nelumbo.zoo_api.validation.annotations.ZonaExist;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public record AnimalRequest(
        @NotBlank
        String name,
        //Hay casos donde se eliminan si no tiene especie o zona (entonces pueden ser nulos)
        @SpeciesExists
        Long speciesId,
        @ZonaExist
        Long zoneId)
{}
