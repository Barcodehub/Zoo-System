package com.nelumbo.zoo_api.dto;

import java.util.List;

public record ZoneResponse(Long id, String name, List<AnimalSimpleResponse> animals) {}
