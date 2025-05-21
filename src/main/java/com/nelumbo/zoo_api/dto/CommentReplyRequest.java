package com.nelumbo.zoo_api.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentReplyRequest(
        @NotBlank
        String message) {}
