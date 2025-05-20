package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.AnimalCountBySpecies;
import com.nelumbo.zoo_api.dto.AnimalCountByZone;
import com.nelumbo.zoo_api.dto.AnimalDetailResponse;
import com.nelumbo.zoo_api.dto.SearchResults;
import com.nelumbo.zoo_api.services.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/animals-by-zone")
    public ResponseEntity<List<AnimalCountByZone>> getAnimalsByZone() {
        return ResponseEntity.ok(statsService.countAnimalsByZone());
    }

    @GetMapping("/answered-comments-percentage")
    public ResponseEntity<Double> getAnsweredCommentsPercentage() {
        return ResponseEntity.ok(statsService.calculateAnsweredCommentsPercentage());
    }

    @GetMapping("/animals-by-species")
    public ResponseEntity<List<AnimalCountBySpecies>> getAnimalsBySpecies() {
        return ResponseEntity.ok(statsService.countAnimalsBySpecies());
    }

    @GetMapping("/animals-by-date")
    public ResponseEntity<List<AnimalDetailResponse>> getAnimalsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statsService.getAnimalsByRegistrationDate(date));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResults> search(
            @RequestParam String query) {
        return ResponseEntity.ok(statsService.searchAll(query));
    }
}