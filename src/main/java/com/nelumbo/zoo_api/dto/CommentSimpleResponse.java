package com.nelumbo.zoo_api.dto;

public record CommentSimpleResponse(Long id, String message, String author, String createdAt) {}