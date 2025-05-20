package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.AnimalSimpleResponse;
import com.nelumbo.zoo_api.dto.ZoneRequest;
import com.nelumbo.zoo_api.dto.ZoneResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.exception.ResourceAlreadyExistsException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final AnimalRepository animalRepository;

    public SuccessResponseDTO<ZoneResponse> createZone(ZoneRequest request) {
        if (zoneRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Zone with this name already exists");
        }

        Zone zone = new Zone();
        zone.setName(request.name());
        zone = zoneRepository.save(zone);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<List<ZoneResponse>> getAllZones() {
        return new SuccessResponseDTO<>(
                zoneRepository.findAll().stream()
                .map(this::mapToZoneResponse)
                .toList()
                );
    }

    public SuccessResponseDTO<ZoneResponse> getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<ZoneResponse> updateZone(Long id, ZoneRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        if (!zone.getName().equals(request.name()) &&
                zoneRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Zone with this name already exists");
        }

        zone.setName(request.name());
        zone = zoneRepository.save(zone);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<Void> deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        if (animalRepository.existsByZone(zone)) {
            throw new IllegalOperationException("Cannot delete zone with associated animals");
        }

        zoneRepository.delete(zone);
        return new SuccessResponseDTO<>(null);
    }

    private ZoneResponse mapToZoneResponse(Zone zone) {
        List<AnimalSimpleResponse> animalResponses = zone.getAnimals().stream()
                .map(animal -> new AnimalSimpleResponse(animal.getId(), animal.getName()))
                .toList();

        return new ZoneResponse(zone.getId(), zone.getName(), animalResponses);
    }
}