package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.SpeciesRequest;
import com.nelumbo.zoo_api.dto.SpeciesResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.services.SpeciesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {
    private final SpeciesService speciesService;

    @PostMapping
    public ResponseEntity<SuccessResponseDTO<SpeciesResponse>> createSpecies(@Valid @RequestBody SpeciesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speciesService.createSpecies(request));
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDTO<List<SpeciesResponse>>> getAllSpecies() {
        return ResponseEntity.ok(speciesService.getAllSpecies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<SpeciesResponse>> getSpeciesById(@PathVariable Long id) {
        return ResponseEntity.ok(speciesService.getSpeciesById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<SpeciesResponse>> updateSpecies(
            @Valid @PathVariable Long id,
            @RequestBody SpeciesRequest request) {
        return ResponseEntity.ok(speciesService.updateSpecies(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<Void>> deleteSpecies(@PathVariable Long id) {
        speciesService.deleteSpecies(id);
        return ResponseEntity.noContent().build();
    }
}