package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final SpeciesRepository speciesRepository;
    private final ZoneRepository zoneRepository;
    private final CommentRepository commentRepository;

    public SuccessResponseDTO<AnimalResponse> createAnimal(@Valid AnimalRequest request) {
        Species species = request.speciesId() != null ?
                speciesRepository.findById(request.speciesId()).orElse(null) : null;

        Zone zone = request.zoneId() != null ?
                zoneRepository.findById(request.zoneId()).orElse(null) : null;

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

    public SuccessResponseDTO<AnimalResponse> getAnimalById(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "id"));
        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<AnimalResponse> updateAnimal(Long id, AnimalRequest request) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "id"));
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

    public SuccessResponseDTO<Void> deleteAnimal(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "id"));
        animalRepository.delete(animal);
        return new SuccessResponseDTO<>(null);
    }

    public SuccessResponseDTO<List<AnimalResponse>> getAnimalsByDate(LocalDate date) {
        Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

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

        return new AnimalResponse(
                animal.getId(),
                animal.getName(),
                mapToSpeciesSimpleResponse(animal.getSpecies()),
                mapToZoneSimpleResponse(animal.getZone()),
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