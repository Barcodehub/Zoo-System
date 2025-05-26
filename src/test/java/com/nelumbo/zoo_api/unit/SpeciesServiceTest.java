package com.nelumbo.zoo_api.unit;

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
import com.nelumbo.zoo_api.services.SpeciesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesServiceTest {

    @Mock
    private SpeciesRepository specieRepository;

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private SpeciesService speciesService;

    // Test para createSpecies
    @Test
    void createSpecies_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        SpeciesRequest request = new SpeciesRequest("Reptil");
        Species savedSpecies = new Species(1L, "Reptil", new ArrayList<>());

        when(specieRepository.save(any(Species.class))).thenReturn(savedSpecies);

        // Act
        SuccessResponseDTO<SpeciesResponse> response = speciesService.createSpecies(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.data().id());
        assertEquals("Reptil", response.data().name());
        verify(specieRepository).save(any(Species.class));
    }

    // Test para getAllSpecies
    @Test
    void getAllSpecies_ShouldReturnListOfSpecies() {
        // Arrange
        List<Species> species = List.of(
                new Species(1L, "Reptil", new ArrayList<>()),
                new Species(2L, "Anfibio", new ArrayList<>())
        );

        when(specieRepository.findAll()).thenReturn(species);

        // Act
        SuccessResponseDTO<List<SpeciesResponse>> response = speciesService.getAllSpecies();

        // Assert
        assertEquals(2, response.data().size());
        verify(specieRepository).findAll();
    }

    @Test
    void getAllSpecies_WhenEmpty_ShouldReturnNullDataAndNoSpeciesMessage() {
        // Arrange
        when(specieRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        SuccessResponseDTO<List<SpeciesResponse>> response = speciesService.getAllSpecies();

        // Assert
        assertNull(response.data());
        assertEquals(ResponseMessages.NO_SPECIES, response.errors());
        verify(specieRepository).findAll();
    }


    // Test para getSpeciesById
    @Test
    void getSpeciesById_WithExistingId_ShouldReturnSpecies() {
        // Arrange
        Species specie = new Species(1L, "Reptil", new ArrayList<>());
        when(specieRepository.findById(1L)).thenReturn(Optional.of(specie));

        // Act
        SuccessResponseDTO<SpeciesResponse> response = speciesService.getSpeciesById(1L);

        // Assert
        assertEquals(1L, response.data().id());
        assertEquals("Reptil", response.data().name());
    }

    @Test
    void getSpeciesById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(specieRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            speciesService.getSpeciesById(99L);
        });
    }

    // Test para updateSpecies
    @Test
    void updateSpecies_WithValidRequest_ShouldReturnUpdatedSpecies() {
        // Arrange
        Species existingSpecies = new Species(1L, "Old Name", new ArrayList<>());
        SpeciesRequest request = new SpeciesRequest("New Name");

        when(specieRepository.findById(1L)).thenReturn(Optional.of(existingSpecies));
        when(specieRepository.save(any(Species.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SuccessResponseDTO<SpeciesResponse> response = speciesService.updateSpecies(1L, request);

        // Assert
        assertEquals("New Name", response.data().name());
        verify(specieRepository).save(existingSpecies);
    }

    // Test para deleteSpecies
    @Test
    void deleteSpecies_WithNoAssociatedAnimals_ShouldDeleteSpecies() {
        // Arrange
        Species species = new Species(1L, "Reptil", new ArrayList<>());
        when(specieRepository.findById(1L)).thenReturn(Optional.of(species));
        when(animalRepository.existsBySpecies(species)).thenReturn(false);

        // Act
        speciesService.deleteSpecies(1L);

        // Assert
        verify(specieRepository).delete(species);
    }

    @Test
    void deleteSpecies_WithAssociatedAnimals_ShouldThrowException() {
        // Arrange
        Species species = new Species(1L, "Reptil", new ArrayList<>());
        when(specieRepository.findById(1L)).thenReturn(Optional.of(species));
        when(animalRepository.existsBySpecies(species)).thenReturn(true);

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class, () -> {
            speciesService.deleteSpecies(1L);
        });

        assertEquals("Cannot delete species with associated animals", exception.getMessage(), exception.getField());
        verify(specieRepository, never()).delete(any());
    }

    @Test
    void updateSpecies_WithNullId_ShouldThrowBadRequestException() {
        // Arrange
        SpeciesRequest request = new SpeciesRequest("New Name");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            speciesService.updateSpecies(null, request);
        });

        // Verifica el mensaje o campo si quieres
        assertEquals("ID inválido: no puede ser nulo", exception.getMessage());
        assertEquals("id", exception.getField());
    }

}