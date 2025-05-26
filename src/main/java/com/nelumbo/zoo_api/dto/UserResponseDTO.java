package com.nelumbo.zoo_api.dto;


import com.nelumbo.zoo_api.models.Role;

public record UserResponseDTO(
        Integer id,
        String name,
        String email,
        Role role
) {}