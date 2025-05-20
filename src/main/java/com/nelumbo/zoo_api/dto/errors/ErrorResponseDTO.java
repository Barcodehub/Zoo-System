package com.nelumbo.zoo_api.dto.errors;

import java.util.List;

public record ErrorResponseDTO(
        Object data,
        List<ErrorDetailDTO> errors
) {}

