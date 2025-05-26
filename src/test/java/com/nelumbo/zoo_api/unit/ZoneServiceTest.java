package com.nelumbo.zoo_api.unit;

import com.nelumbo.zoo_api.dto.ZoneRequest;
import com.nelumbo.zoo_api.dto.ZoneResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private ZoneService zoneService;

    // Test para createZone
    @Test
    void createZone_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        ZoneRequest request = new ZoneRequest("Savannah");
        Zone savedZone = new Zone(1L, "Savannah", new ArrayList<>());

        when(zoneRepository.save(any(Zone.class))).thenReturn(savedZone);

        // Act
        SuccessResponseDTO<ZoneResponse> response = zoneService.createZone(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.data().id());
        assertEquals("Savannah", response.data().name());
        verify(zoneRepository).save(any(Zone.class));
    }

    // Test para getAllZones
    @Test
    void getAllZones_ShouldReturnListOfZones() {
        // Arrange
        List<Zone> zones = List.of(
                new Zone(1L, "Savannah", new ArrayList<>()),
                new Zone(2L, "Jungle", new ArrayList<>())
        );

        when(zoneRepository.findAll()).thenReturn(zones);

        // Act
        SuccessResponseDTO<List<ZoneResponse>> response = zoneService.getAllZones();

        // Assert
        assertEquals(2, response.data().size());
        verify(zoneRepository).findAll();
    }

    // Test para getZoneById
    @Test
    void getZoneById_WithExistingId_ShouldReturnZone() {
        // Arrange
        Zone zone = new Zone(1L, "Savannah", new ArrayList<>());
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

        // Act
        SuccessResponseDTO<ZoneResponse> response = zoneService.getZoneById(1L);

        // Assert
        assertEquals(1L, response.data().id());
        assertEquals("Savannah", response.data().name());
    }

    @Test
    void getZoneById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            zoneService.getZoneById(99L);
        });
    }

    // Test para updateZone
    @Test
    void updateZone_WithValidRequest_ShouldReturnUpdatedZone() {
        // Arrange
        Zone existingZone = new Zone(1L, "Old Name", new ArrayList<>());
        ZoneRequest request = new ZoneRequest("New Name");

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(existingZone));
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SuccessResponseDTO<ZoneResponse> response = zoneService.updateZone(1L, request);

        // Assert
        assertEquals("New Name", response.data().name());
        verify(zoneRepository).save(existingZone);
    }

    // Test para deleteZone
    @Test
    void deleteZone_WithNoAssociatedAnimals_ShouldDeleteZone() {
        // Arrange
        Zone zone = new Zone(1L, "Savannah", new ArrayList<>());
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(animalRepository.existsByZone(zone)).thenReturn(false);

        // Act
        zoneService.deleteZone(1L);

        // Assert
        verify(zoneRepository).delete(zone);
    }

    @Test
    void deleteZone_WithAssociatedAnimals_ShouldThrowException() {
        // Arrange
        Zone zone = new Zone(1L, "Savannah", new ArrayList<>());
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(animalRepository.existsByZone(zone)).thenReturn(true);

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class, () -> {
            zoneService.deleteZone(1L);
        });

        assertEquals("Cannot delete zone with associated animals", exception.getMessage());
        verify(zoneRepository, never()).delete(any());
    }
}