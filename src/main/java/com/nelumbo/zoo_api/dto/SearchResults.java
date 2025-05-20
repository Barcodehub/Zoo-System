package com.nelumbo.zoo_api.dto;

import java.util.List;

public record SearchResults(
        List<ZoneSearchResult> zones,
        List<AnimalSearchResult> animals,
        List<CommentSearchResult> comments,
        List<ReplySearchResult> replies
) {}