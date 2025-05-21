package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.AnimalRequest;
import com.nelumbo.zoo_api.dto.AnimalResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.services.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
public class AnimalController {
    private final AnimalService animalService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponseDTO<AnimalResponse> createAnimal(@Valid @RequestBody AnimalRequest request) {
        return animalService.createAnimal(request);
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDTO<List<AnimalResponse>>> getAllAnimals() {
        return ResponseEntity.ok(animalService.getAllAnimals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<AnimalResponse>> getAnimalById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.getAnimalById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<AnimalResponse>> updateAnimal(
            @PathVariable Long id,
            @RequestBody AnimalRequest request) {
        return ResponseEntity.ok(animalService.updateAnimal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<Void>> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-date")
    public ResponseEntity<SuccessResponseDTO<List<AnimalResponse>>> getAnimalsByDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(animalService.getAnimalsByDate(localDate));
    }
}