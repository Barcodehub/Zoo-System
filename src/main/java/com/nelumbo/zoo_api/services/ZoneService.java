package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.AnimalSimpleResponse;
import com.nelumbo.zoo_api.dto.ZoneRequest;
import com.nelumbo.zoo_api.dto.ZoneResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.validation.annotations.ZonaExist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final AnimalRepository animalRepository;

    public SuccessResponseDTO<ZoneResponse> createZone(@Valid ZoneRequest request) {
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

    public SuccessResponseDTO<ZoneResponse> getZoneById(@ZonaExist Long id) {
        Zone zone = zoneRepository.getReferenceById(id);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<ZoneResponse> updateZone(@Valid @ZonaExist Long id, ZoneRequest request) {
        Zone zone = zoneRepository.getReferenceById(id);
        zone.setName(request.name());
        zone = zoneRepository.save(zone);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<Void> deleteZone(@ZonaExist Long id) {
        Zone zone = zoneRepository.getReferenceById(id);

        if (animalRepository.existsByZone(zone)) {
            throw new IllegalOperationException("Cannot delete zone with associated animals", null);
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