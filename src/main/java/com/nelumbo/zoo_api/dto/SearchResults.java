package com.nelumbo.zoo_api.dto;

import java.util.List;

public record SearchResults(
        List<ZoneSearchResult> zones,
        List<AnimalDetailResponse> animals,
        List<SpecieSearchResult> species,
        List<CommentSearchResult> comments,
        List<ReplySearchResult> replies
) {}