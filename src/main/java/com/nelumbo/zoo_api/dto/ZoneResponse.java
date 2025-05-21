package com.nelumbo.zoo_api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ZoneResponse(Long id, @NotBlank String name, List<AnimalSimpleResponse> animals) {}
