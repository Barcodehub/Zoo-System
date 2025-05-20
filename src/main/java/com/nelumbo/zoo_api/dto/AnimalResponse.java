package com.nelumbo.zoo_api.dto;

import java.util.List;

public record AnimalResponse(
        Long id,
        String name,
        SpeciesSimpleResponse species,
        ZoneSimpleResponse zone,
        String registrationDate,
        List<CommentSimpleResponse> comments
) {}