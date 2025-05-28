package com.nelumbo.zoo_api.unit;

import com.nelumbo.zoo_api.dto.AnimalRequest;
import com.nelumbo.zoo_api.dto.AnimalResponse;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.services.AnimalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpeciesRepository speciesRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private AnimalService animalService;

    @Test
    void createAnimal_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        AnimalRequest request = new AnimalRequest("León", 1L, 2L);
        Species species = new Species(1L, "Felino", new ArrayList<>());
        Zone zone = new Zone(2L, "Sabana", new ArrayList<>());
        Animal savedAnimal = new Animal(1L, "León", species, zone, new Date(), new ArrayList<>());

        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(zone));
        when(animalRepository.save(any(Animal.class))).thenReturn(savedAnimal);

        // Act
        SuccessResponseDTO<AnimalResponse> response = animalService.createAnimal(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.data().id());
        assertEquals("León", response.data().name());
        assertEquals("Felino", response.data().species().name());
        assertEquals("Sabana", response.data().zone().name());

        verify(speciesRepository).findById(1L);
        verify(zoneRepository).findById(2L);
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    void createAnimal_WithNonexistentSpecies_ShouldThrowException() {
        // Arrange
        AnimalRequest request = new AnimalRequest("León", 99L, 2L);
        when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> animalService.createAnimal(request));

        verify(speciesRepository).findById(99L);
        verify(zoneRepository, never()).findById(any());
        verify(animalRepository, never()).save(any());
    }

    @Test
    void getAllAnimals_WhenNoAnimalsExist_ShouldReturnEmptyResponse() {
        // Arrange
        when(animalRepository.findAllWithRelations()).thenReturn(new ArrayList<>());

        // Act
        SuccessResponseDTO<List<AnimalResponse>> response = animalService.getAllAnimals();

        // Assert
        assertNull(response.data());
        assertEquals(ResponseMessages.NO_ANIMALS, response.errors());
        verify(animalRepository).findAllWithRelations();
    }

    @Test
    void updateAnimal_WithValidRequest_ShouldUpdateSuccessfully() {
        // Arrange
        Long animalId = 1L;
        AnimalRequest request = new AnimalRequest("León Actualizado", 1L, 3L);
        Animal existingAnimal = new Animal(animalId, "León",
                new Species(1L, "Felino", new ArrayList<>()),
                new Zone(2L, "Sabana", new ArrayList<>()),
                new Date(), new ArrayList<>());
        Zone newZone = new Zone(3L, "Nueva Zona", new ArrayList<>());

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(existingAnimal));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(existingAnimal.getSpecies()));
        when(zoneRepository.findById(3L)).thenReturn(Optional.of(newZone));
        when(animalRepository.save(any(Animal.class))).thenReturn(existingAnimal);

        // Act
        SuccessResponseDTO<AnimalResponse> response = animalService.updateAnimal(animalId, request);

        // Assert
        assertEquals(animalId, response.data().id());
        assertEquals("León Actualizado", response.data().name());
        assertEquals("Nueva Zona", response.data().zone().name());

        verify(animalRepository).findById(animalId);
        verify(zoneRepository).findById(3L);
        verify(animalRepository).save(existingAnimal);
    }

    @Test
    void deleteAnimal_WithExistingId_ShouldDeleteSuccessfully() {
        // Arrange
        Long animalId = 1L;
        Animal existingAnimal = new Animal(animalId, "León", null, null, new Date(), new ArrayList<>());
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(existingAnimal));

        // Act
        animalService.deleteAnimal(animalId);

        // Assert
        verify(animalRepository).findById(animalId);
        verify(animalRepository).delete(existingAnimal);
    }

    @Test
    void getAnimalById_WhenAnimalExists_ShouldReturnAnimalResponse() {
        // Arrange
        Long animalId = 1L;
        Species species = new Species(1L, "Felino", new ArrayList<>());
        Zone zone = new Zone(1L, "Sabana", new ArrayList<>());
        Animal animal = new Animal(animalId, "León", species, zone, new Date(), new ArrayList<>());

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // Act
        SuccessResponseDTO<AnimalResponse> response = animalService.getAnimalById(animalId);

        // Assert
        assertNotNull(response);
        assertNull(response.errors());
        assertEquals(animalId, response.data().id());
        assertEquals("León", response.data().name());
        assertEquals("Felino", response.data().species().name());
        assertEquals("Sabana", response.data().zone().name());

        verify(animalRepository).findById(animalId);
    }


    @Test
    void getAnimalById_WhenAnimalNotExists_ShouldThrowException() {
        // Arrange
        Long nonExistentId = 99L;
        when(animalRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> animalService.getAnimalById(nonExistentId));

        verify(animalRepository).findById(nonExistentId);
    }



}