package com.nelumbo.zoo_api.dto;

import java.util.List;

public record CommentResponse2(
        Long id,
        String message,
        String author,
        String createdAt,
        Long animalId,
        String animalName,  // Nuevo campo para mostrar info del animal
        Long zoneId,        // Nuevo campo para relación con zona
        String zoneName,    // Nuevo campo para mostrar info de la zona
        Long parentCommentId,
        List<CommentResponse2> replies
) {
}
