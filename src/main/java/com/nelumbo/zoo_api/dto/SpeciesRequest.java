package com.nelumbo.zoo_api.dto;

import com.nelumbo.zoo_api.validation.annotations.UniqueSpeciesName;
import jakarta.validation.constraints.NotBlank;


public record SpeciesRequest(@NotBlank @UniqueSpeciesName String name) {}
