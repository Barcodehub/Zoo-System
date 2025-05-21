package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
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
    public ResponseEntity<SuccessResponseDTO<List<AnimalCountByZone>>> getAnimalsByZone() {
        return ResponseEntity.ok(statsService.countAnimalsByZone());
    }

    @GetMapping("/answered-comments-percentage")
    public ResponseEntity<SuccessResponseDTO<Double>> getAnsweredCommentsPercentage() {
        return ResponseEntity.ok(statsService.calculateAnsweredCommentsPercentage());
    }

    @GetMapping("/animals-by-species")
    public ResponseEntity<SuccessResponseDTO<List<AnimalCountBySpecies>>> getAnimalsBySpecies() {
        return ResponseEntity.ok(statsService.countAnimalsBySpecies());
    }

    @GetMapping("/animals-by-date")
    public ResponseEntity<SuccessResponseDTO<List<AnimalResponse>>> getAnimalsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statsService.getAnimalsByRegistrationDate(date));
    }

    @GetMapping("/search")
    public ResponseEntity<SuccessResponseDTO<SearchResults>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(statsService.searchAll(query));
    }
}