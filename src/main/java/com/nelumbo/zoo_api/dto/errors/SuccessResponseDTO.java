package com.nelumbo.zoo_api.dto.errors;

public record SuccessResponseDTO<T>(
        T data,
        Object errors // Siempre null en éxito
) {
    public SuccessResponseDTO(T data) {
        this(data, null);
    }
}