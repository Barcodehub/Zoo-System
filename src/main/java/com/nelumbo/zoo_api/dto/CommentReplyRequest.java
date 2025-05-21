package com.nelumbo.zoo_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentReplyRequest(
        @NotBlank
        String message) {}
