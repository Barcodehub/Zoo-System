package com.nelumbo.zoo_api.dto;

import com.nelumbo.zoo_api.validation.annotations.UniqueZoneName;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public record ZoneRequest(@NotBlank @UniqueZoneName String name) {}

