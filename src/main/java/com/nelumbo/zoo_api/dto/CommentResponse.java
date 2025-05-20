package com.nelumbo.zoo_api.dto;

import java.util.List;

public record CommentResponse(
        Long id,
        String message,
        String author,
        String createdAt,
        Long animalId,
        Long parentCommentId,
        List<CommentResponse> replies
) {}