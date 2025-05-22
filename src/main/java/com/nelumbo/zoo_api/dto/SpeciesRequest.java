package com.nelumbo.zoo_api.dto;

import com.nelumbo.zoo_api.validation.annotations.UniqueSpeciesName;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;


public record SpeciesRequest(@NotBlank @UniqueSpeciesName String name) {}
