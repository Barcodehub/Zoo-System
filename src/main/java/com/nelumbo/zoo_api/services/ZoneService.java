package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.AnimalSimpleResponse;
import com.nelumbo.zoo_api.dto.ZoneRequest;
import com.nelumbo.zoo_api.dto.ZoneResponse;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.BadRequestException;
import com.nelumbo.zoo_api.exception.IllegalOperationException;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
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

    public SuccessResponseDTO<ZoneResponse> getZoneById(Long id) {
        Zone zone = zoneExist(id);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public SuccessResponseDTO<ZoneResponse> updateZone(@Valid Long id, @Valid ZoneRequest request) {
        Zone zone = zoneExist(id);
        zone.setName(request.name());
        zone = zoneRepository.save(zone);
        return new SuccessResponseDTO<>(mapToZoneResponse(zone));
    }

    public void deleteZone(Long id) {
        Zone zone = zoneExist(id);

        if (animalRepository.existsByZone(zone)) {
            throw new IllegalOperationException("Cannot delete zone with associated animals", null);
        }

        zoneRepository.delete(zone);
    }

    private ZoneResponse mapToZoneResponse(Zone zone) {
        List<AnimalSimpleResponse> animalResponses = zone.getAnimals().stream()
                .map(animal -> new AnimalSimpleResponse(animal.getId(), animal.getName()))
                .toList();

        return new ZoneResponse(zone.getId(), zone.getName(), animalResponses);
    }

    private Zone zoneExist(Long zoneId) {
        if (zoneId == null) {
            throw new BadRequestException("ID inválido: no puede ser nulo", "id");
        }
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada", "id"));
    }

}