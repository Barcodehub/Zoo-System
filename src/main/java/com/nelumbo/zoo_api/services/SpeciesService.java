package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.SpeciesRequest;
import com.nelumbo.zoo_api.dto.SpeciesResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.validation.annotations.SpeciesExists;
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
        return  new SuccessResponseDTO<>(speciesRepository.findAll().stream()
                .map(species -> mapToSpeciesResponse(species))
                .toList());
    }

    public SuccessResponseDTO<SpeciesResponse> getSpeciesById(@SpeciesExists Long id) {
        Species species = speciesRepository.getReferenceById(id);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<SpeciesResponse> updateSpecies(@Valid @SpeciesExists Long id, @Valid SpeciesRequest request) {
        Species species = speciesRepository.getReferenceById(id);

        species.setName(request.name());
        species = speciesRepository.save(species);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<Void> deleteSpecies(@SpeciesExists Long id) {
        Species species = speciesRepository.getReferenceById(id);

        if (animalRepository.existsBySpecies(species)) {
            throw new IllegalOperationException("Cannot delete species with associated animals", null);
        }

        speciesRepository.delete(species);
        return new SuccessResponseDTO<>(null);
    }


    public SpeciesResponse mapToSpeciesResponse(Species species) {
        return new SpeciesResponse(species.getId(), species.getName());
    }
}