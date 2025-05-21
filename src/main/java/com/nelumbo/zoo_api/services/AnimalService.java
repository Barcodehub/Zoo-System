package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.validation.annotations.AnimalExists;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final SpeciesRepository speciesRepository;
    private final ZoneRepository zoneRepository;
    private final CommentRepository commentRepository;

    public SuccessResponseDTO<AnimalResponse> createAnimal(@Valid AnimalRequest request) {
        Species species = request.speciesId() != null
                ? speciesRepository.getReferenceById(request.speciesId())
                : null;

        Zone zone = request.zoneId() != null
                ? zoneRepository.getReferenceById(request.zoneId())
                : null;

        Animal animal = new Animal();
        animal.setName(request.name());
        animal.setSpecies(species);
        animal.setZone(zone);
        animal.setRegistrationDate(new Date());
        animal = animalRepository.save(animal);

        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<List<AnimalResponse>> getAllAnimals() {
        return new SuccessResponseDTO<>(animalRepository.findAll().stream()
                .map(this::mapToAnimalResponse)
                .toList());
    }

    public SuccessResponseDTO<AnimalResponse> getAnimalById(@AnimalExists Long id) {
        Animal animal = animalRepository.getReferenceById(id);
        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<AnimalResponse> updateAnimal(@AnimalExists Long id, @Valid AnimalRequest request) {
        Animal animal = animalRepository.getReferenceById(id);
        Species species = request.speciesId() != null ?
                speciesRepository.findById(request.speciesId()).orElse(null) : null;

        Zone zone = request.zoneId() != null ?
                zoneRepository.findById(request.zoneId()).orElse(null) : null;

        animal.setName(request.name());
        animal.setSpecies(species);
        animal.setZone(zone);
        animal = animalRepository.save(animal);

        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<Void> deleteAnimal(@AnimalExists Long id) {
        Animal animal = animalRepository.getReferenceById(id);
        animalRepository.delete(animal);
        return new SuccessResponseDTO<>(null);
    }

    public SuccessResponseDTO<List<AnimalResponse>> getAnimalsByDate(LocalDate date) {
        Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        return new SuccessResponseDTO<>(animalRepository.findByRegistrationDateBetween(startDate, endDate).stream()
                .map(this::mapToAnimalResponse)
                .toList());
    }

    private AnimalResponse mapToAnimalResponse(Animal animal) {
        List<CommentSimpleResponse> comments = animal.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> new CommentSimpleResponse(
                        comment.getId(),
                        comment.getMessage(),
                        comment.getAuthor(),
                        comment.getCreatedAt().toString()
                ))
                .toList();

        SpeciesSimpleResponse speciesResponse = animal.getSpecies() != null
                ? mapToSpeciesSimpleResponse(animal.getSpecies())
                : null;

        ZoneSimpleResponse zoneResponse = animal.getZone() != null
                ? mapToZoneSimpleResponse(animal.getZone())
                : null;

        return new AnimalResponse(
                animal.getId(),
                animal.getName(),
                speciesResponse,
                zoneResponse,
                animal.getRegistrationDate().toString(),
                comments
        );
    }


    private SpeciesSimpleResponse mapToSpeciesSimpleResponse(Species species) {
        return new SpeciesSimpleResponse(species.getId(), species.getName());
    }

    private ZoneSimpleResponse mapToZoneSimpleResponse(Zone zone) {
        return new ZoneSimpleResponse(zone.getId(), zone.getName());
    }
}