package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.SpeciesRequest;
import com.nelumbo.zoo_api.dto.SpeciesResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.exception.ResourceAlreadyExistsException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final AnimalRepository animalRepository;

    public SuccessResponseDTO<SpeciesResponse> createSpecies(SpeciesRequest request) {
        if (speciesRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Species with this name already exists");
        }

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

    public SuccessResponseDTO<SpeciesResponse> getSpeciesById(Long id) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Species not found"));
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<SpeciesResponse> updateSpecies(Long id, SpeciesRequest request) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Species not found"));

        if (!species.getName().equals(request.name()) &&
                speciesRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Species with this name already exists");
        }

        species.setName(request.name());
        species = speciesRepository.save(species);
        return new SuccessResponseDTO<>(mapToSpeciesResponse(species));
    }

    public SuccessResponseDTO<Void> deleteSpecies(Long id) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Species not found"));

        if (animalRepository.existsBySpecies(species)) {
            throw new IllegalOperationException("Cannot delete species with associated animals");
        }

        speciesRepository.delete(species);
        return new SuccessResponseDTO<>(null);
    }


    public SpeciesResponse mapToSpeciesResponse(Species species) {
        return new SpeciesResponse(species.getId(), species.getName());
    }
}