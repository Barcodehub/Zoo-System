package com.nelumbo.zoo_api.dto.errors;

public record ErrorDetailDTO(
        String code,
        String description,
        String field
) {}