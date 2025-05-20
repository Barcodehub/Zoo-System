package com.nelumbo.zoo_api.dto;

import com.nelumbo.zoo_api.models.Role;

public record AuthResponseDTO(
        String token,
        String email,
        Role role
) {}