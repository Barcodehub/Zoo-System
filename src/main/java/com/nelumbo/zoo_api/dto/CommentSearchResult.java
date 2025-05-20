package com.nelumbo.zoo_api.dto;

public record CommentSearchResult(
        Long id,
        String message,
        String author,
        String createdAt,
        Long animalId,
        String animalName
) {}