package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.BadRequestException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final SpeciesRepository speciesRepository;
    private final ZoneRepository zoneRepository;

    //validar que existan
    public SuccessResponseDTO<AnimalResponse> createAnimal(@Valid AnimalRequest request) {

        Species species = request.speciesId() != null ? specieExist(request.speciesId()) : null;
        Zone zone = request.zoneId() != null ? zoneExist(request.zoneId()) : null;

        Animal animal = new Animal();
        animal.setName(request.name());
        animal.setSpecies(species);
        animal.setZone(zone);
        animal.setRegistrationDate(new Date());
        animal = animalRepository.save(animal);

        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<List<AnimalResponse>> getAllAnimals() {
        List<AnimalResponse> result = animalRepository.findAll().stream()
                .map(this::mapToAnimalResponse)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_ANIMALS)
                : new SuccessResponseDTO<>(result);
    }

    public SuccessResponseDTO<AnimalResponse> getAnimalById(Long id) {
       Animal animal = animalExist(id);
        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<AnimalResponse> updateAnimal(Long id, @Valid AnimalRequest request) {
        Animal animal = animalExist(id);
        Species species = request.speciesId() != null ? specieExist(request.speciesId()) : null;
        Zone zone = request.zoneId() != null ? zoneExist(request.zoneId()) : null;

        animal.setName(request.name());
        animal.setSpecies(species);
        animal.setZone(zone);
        animal = animalRepository.save(animal);

        return new SuccessResponseDTO<>(mapToAnimalResponse(animal));
    }

    public SuccessResponseDTO<Void> deleteAnimal(Long id) {
        Animal animal = animalExist(id);
        animalRepository.delete(animal);
        return new SuccessResponseDTO<>(null);
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

    private Animal animalExist(Long animalId) {
        if (animalId == null) {
            throw new BadRequestException("ID inválido: no puede ser nulo", "id");
        }
        return animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal no encontrado", "id"));
    }

    private Zone zoneExist(Long zoneId) {
        if (zoneId == null) return null;
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada", "zoneId"));
    }

    private Species specieExist(Long specieId) {
        if (specieId == null) return null;
        return speciesRepository.findById(specieId)
                .orElseThrow(() -> new ResourceNotFoundException("Especie no encontrada", "speciesId"));
    }

    private SpeciesSimpleResponse mapToSpeciesSimpleResponse(Species species) {
        return new SpeciesSimpleResponse(species.getId(), species.getName());
    }

    private ZoneSimpleResponse mapToZoneSimpleResponse(Zone zone) {
        return new ZoneSimpleResponse(zone.getId(), zone.getName());
    }
}