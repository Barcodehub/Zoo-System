package com.nelumbo.zoo_api.dto;

public record ReplySearchResult(
        Long id,
        String message,
        String author,
        String createdAt,
        Long commentId,
        String commentMessage,
        Long animalId,
        String animalName
) {}