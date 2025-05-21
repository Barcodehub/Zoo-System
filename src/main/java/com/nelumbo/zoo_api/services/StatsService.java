package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Comment;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final AnimalRepository animalRepository;
    private final CommentRepository commentRepository;
    private final ZoneRepository zoneRepository;
    private final SpeciesRepository speciesRepository;

    // 1. Cantidad de animales por zona
    public SuccessResponseDTO<List<AnimalCountByZone>> countAnimalsByZone() {
        return new SuccessResponseDTO<>( zoneRepository.findAll().stream()
                .map(zone -> new AnimalCountByZone(
                        zone.getName(),
                        animalRepository.countByZone(zone)
                ))
                .toList());
    }

    // 2. Porcentaje de comentarios con respuestas
    public SuccessResponseDTO<Double> calculateAnsweredCommentsPercentage() {
        long totalComments = commentRepository.countByParentCommentIsNull();
        if (totalComments == 0) return new SuccessResponseDTO<>(0.0);

        long answeredComments = commentRepository.countByParentCommentIsNotNull();
        double percentage = (answeredComments * 100.0) / totalComments;
        // Redondear a 2 decimales
        double roundedPercentage = Math.round(percentage * 100.0) / 100.0;
        return new SuccessResponseDTO<>(roundedPercentage);
    }

    // 3. Cantidad de animales por especie
    public SuccessResponseDTO<List<AnimalCountBySpecies>> countAnimalsBySpecies() {
        return new SuccessResponseDTO<>( speciesRepository.findAll().stream()
                .map(species -> new AnimalCountBySpecies(
                        species.getName(),
                        animalRepository.countBySpecies(species)
                ))
                .toList());
    }

    // 4. Animales registrados en una fecha específica
    public SuccessResponseDTO<List<AnimalDetailResponse>> getAnimalsByRegistrationDate(LocalDate date) {
        return new SuccessResponseDTO<>( animalRepository.findByRegistrationDate(date).stream()
                .map(this::mapToAnimalDetailResponse)
                .toList());
    }

    // 5. Búsqueda general
    public SuccessResponseDTO<SearchResults> searchAll(String query) {
        List<Zone> zones = zoneRepository.findByNameContainingIgnoreCase(query);
        List<Animal> animals = animalRepository.findByNameContainingIgnoreCase(query);
        List<Comment> comments = commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNull(query);
        List<Comment> replies = commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNotNull(query);

        return new SuccessResponseDTO<>( new SearchResults(
                zones.stream().map(this::mapToZoneSearchResult).toList(),
                animals.stream().map(this::mapToAnimalSearchResult).toList(),
                comments.stream().map(this::mapToCommentSearchResult).toList(),
                replies.stream().map(this::mapToReplySearchResult).toList()
        ));
    }

    // Métodos auxiliares de mapeo
    private AnimalDetailResponse mapToAnimalDetailResponse(Animal animal) {
        return new AnimalDetailResponse(
                animal.getId(),
                animal.getName(),
                animal.getSpecies().getName(),
                animal.getZone().getName(),
                animal.getRegistrationDate().toString()
        );
    }

    private ZoneSearchResult mapToZoneSearchResult(Zone zone) {
        return new ZoneSearchResult(zone.getId(), zone.getName());
    }

    private AnimalSearchResult mapToAnimalSearchResult(Animal animal) {
        return new AnimalSearchResult(
                animal.getId(),
                animal.getName(),
                animal.getSpecies() != null ? animal.getSpecies().getName() : null,
                animal.getZone() != null ? animal.getZone().getName() : null
        );
    }

    private CommentSearchResult mapToCommentSearchResult(Comment comment) {
        return new CommentSearchResult(
                comment.getId(),
                comment.getMessage(),
                comment.getAuthor(),
                comment.getCreatedAt().toString(),
                comment.getAnimal().getId(),
                comment.getAnimal().getName()
        );
    }

    private ReplySearchResult mapToReplySearchResult(Comment reply) {
        return new ReplySearchResult(
                reply.getId(),
                reply.getMessage(),
                reply.getAuthor(),
                reply.getCreatedAt().toString(),
                reply.getParentComment().getId(),
                reply.getParentComment().getMessage(),
                reply.getAnimal().getId(),
                reply.getAnimal().getName()
        );
    }
}