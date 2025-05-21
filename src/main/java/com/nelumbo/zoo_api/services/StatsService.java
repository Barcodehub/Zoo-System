package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.*;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Comment;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
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
        List<AnimalCountByZone> result = zoneRepository.findAll().stream()
                .map(zone -> new AnimalCountByZone(
                        zone.getName(),
                        animalRepository.countByZone(zone)
                ))
                .filter(count -> count.getCount() > 0)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_ANIMALS_ZONE)
                : new SuccessResponseDTO<>(result);
    }

    // 2. Porcentaje de comentarios con respuestas
    public SuccessResponseDTO<Double> calculateAnsweredCommentsPercentage() {
        long totalComments = commentRepository.countByParentCommentIsNull();
        if (totalComments == 0) return new SuccessResponseDTO<>(0.0, ResponseMessages.NO_Reply_COMMENTS);

        long answeredComments = commentRepository.countByParentCommentIsNotNull();
        double percentage = (answeredComments * 100.0) / totalComments;
        double roundedPercentage = Math.round(percentage * 100.0) / 100.0;
        return new SuccessResponseDTO<>(roundedPercentage);
    }

    // 3. Cantidad de animales por especie
    public SuccessResponseDTO<List<AnimalCountBySpecies>> countAnimalsBySpecies() {
        List<AnimalCountBySpecies> result = speciesRepository.findAll().stream()
                .map(species -> new AnimalCountBySpecies(
                        species.getName(),
                        animalRepository.countBySpecies(species))
                )
                .filter(count -> count.getCount() > 0)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_ANIMALS_SPECIES)
                : new SuccessResponseDTO<>(result);
    }

    // 4. Animales registrados en una fecha específica


    public SuccessResponseDTO<List<AnimalResponse>> getAnimalsByRegistrationDate(LocalDate date) {
        Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<AnimalResponse> result = animalRepository.findByRegistrationDateBetween(startDate, endDate).stream()
                .map(this::mapToAnimalResponse)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_ANIMALS_DATE)
                : new SuccessResponseDTO<>(result);
    }

    // 5. Búsqueda general
    public SuccessResponseDTO<SearchResults> searchAll(String query) {
        List<Zone> zones = zoneRepository.findByNameContainingIgnoreCase(query);
        List<Animal> animals = animalRepository.findByNameContainingIgnoreCase(query);
        List<Species> especie = speciesRepository.findByNameContainingIgnoreCase(query);
        List<Comment> comments = commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNull(query);
        List<Comment> replies = commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNotNull(query);

        SearchResults results = new SearchResults(
                zones.stream().map(this::mapToZoneSearchResult).toList(),
                animals.stream().map(this::mapToAnimalDetailResponse).toList(),
                especie.stream().map(this::mapToSpecieSearchResult).toList(),
                comments.stream().map(this::mapToCommentSearchResult).toList(),
                replies.stream().map(this::mapToReplySearchResult).toList()
        );

        boolean isEmpty = results.zones().isEmpty() &&
                results.animals().isEmpty() &&
                results.species().isEmpty() &&
                results.comments().isEmpty() &&
                results.replies().isEmpty();

        return isEmpty
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_RESULTS_SEARCH + query)
                : new SuccessResponseDTO<>(results);
    }

    // Métodos auxiliares de mapeo
    private AnimalDetailResponse mapToAnimalDetailResponse(Animal animal) {
        return new AnimalDetailResponse(
                animal.getId(),
                animal.getName(),
                animal.getSpecies() != null ? animal.getSpecies().getName() : null,
                animal.getZone() != null ? animal.getZone().getName() : null,
                animal.getRegistrationDate().toString()
        );
    }

    private ZoneSearchResult mapToZoneSearchResult(Zone zone) {
        return new ZoneSearchResult(zone.getId(), zone.getName());
    }

    private SpecieSearchResult mapToSpecieSearchResult(Species specie) {
        return new SpecieSearchResult(specie.getId(), specie.getName());
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

    private AnimalResponse mapToAnimalResponse(Animal animal) {
        List<CommentSimpleResponse> comments = animal.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> new CommentSimpleResponse(
                        comment.getId(),
                        comment.getMessage(),
                        comment.getAuthor(),
                        comment.getCreatedAt().toString()
                ))
                .toList();

        SpeciesSimpleResponse speciesResponse = animal.getSpecies() != null
                ? mapToSpeciesSimpleResponse(animal.getSpecies())
                : null;

        ZoneSimpleResponse zoneResponse = animal.getZone() != null
                ? mapToZoneSimpleResponse(animal.getZone())
                : null;

        return new AnimalResponse(
                animal.getId(),
                animal.getName(),
                speciesResponse,
                zoneResponse,
                animal.getRegistrationDate().toString(),
                comments
        );
    }
    private SpeciesSimpleResponse mapToSpeciesSimpleResponse(Species species) {
        return new SpeciesSimpleResponse(species.getId(), species.getName());
    }

    private ZoneSimpleResponse mapToZoneSimpleResponse(Zone zone) {
        return new ZoneSimpleResponse(zone.getId(), zone.getName());
    }


}