package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.SpeciesRequest;
import com.nelumbo.zoo_api.dto.SpeciesResponse;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.BadRequestException;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final AnimalRepository animalRepository;

    public SuccessResponseDTO<SpeciesResponse> createSpecies(@Valid SpeciesRequest request) {
        Species species = new Species();
        species.setName(request.name());
        species = speciesRepository.save(species);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }


    public SuccessResponseDTO<List<SpeciesResponse>> getAllSpecies() {
        List<SpeciesResponse> result = speciesRepository.findAll().stream()
                .map(this::mapToSpeciesResponse)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_SPECIES)
                : new SuccessResponseDTO<>(result);
    }

    public SuccessResponseDTO<SpeciesResponse> getSpeciesById(Long id) {
        Species species = specieExist(id);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<SpeciesResponse> updateSpecies(@Valid Long id, SpeciesRequest request) {
        Species species = specieExist(id);

        species.setName(request.name());
        species = speciesRepository.save(species);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<Void> deleteSpecies(Long id) {
        Species species = specieExist(id);

        if (animalRepository.existsBySpecies(species)) {
            throw new IllegalOperationException("Cannot delete species with associated animals", null);
        }

        speciesRepository.delete(species);
        return new SuccessResponseDTO<>(null);
    }


    public SpeciesResponse mapToSpeciesResponse(Species species) {
        return new SpeciesResponse(species.getId(), species.getName());
    }

    private Species specieExist(Long specieId) {
        if (specieId == null) {
            throw new BadRequestException("ID inválido: no puede ser nulo", "id");
        }
        return speciesRepository.findById(specieId)
                .orElseThrow(() -> new ResourceNotFoundException("Especie no encontrada", "id"));
    }

}