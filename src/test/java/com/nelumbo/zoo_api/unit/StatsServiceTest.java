package com.nelumbo.zoo_api.unit;

import com.nelumbo.zoo_api.dto.AnimalCountBySpecies;
import com.nelumbo.zoo_api.dto.AnimalCountByZone;
import com.nelumbo.zoo_api.dto.AnimalResponse;
import com.nelumbo.zoo_api.dto.SearchResults;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.services.StatsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock private AnimalRepository animalRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private SpeciesRepository speciesRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void countAnimalsByZone_WhenZonesExist_ShouldReturnCounts() {
        // Arrange
        Zone zone1 = new Zone();
        zone1.setId(1L);
        zone1.setName("Savannah");
        Zone zone2 = new Zone();
        zone2.setId(2L);
        zone2.setName("Jungle");

        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));
        when(animalRepository.countByZone(zone1)).thenReturn(5L);
        when(animalRepository.countByZone(zone2)).thenReturn(3L);

        // Act
        SuccessResponseDTO<List<AnimalCountByZone>> result = statsService.countAnimalsByZone();

        // Assert
        assertNotNull(result.data());
        assertEquals(2, result.data().size());
        assertEquals("Savannah", result.data().get(0).zoneName());
        assertEquals(5L, result.data().get(0).animalCount());
        assertEquals("Jungle", result.data().get(1).zoneName());
        assertEquals(3L, result.data().get(1).animalCount());
    }

    @Test
    void countAnimalsByZone_WhenNoAnimals_ShouldReturnEmpty() {
        when(zoneRepository.findAll()).thenReturn(Collections.emptyList());

        SuccessResponseDTO<List<AnimalCountByZone>> result = statsService.countAnimalsByZone();

        assertNull(result.data());
        assertEquals(ResponseMessages.NO_ANIMALS_ZONE, result.errors());
    }

    @Test
    void calculateAnsweredCommentsPercentage_WhenCommentsExist_ShouldCalculatePercentage() {
        when(commentRepository.countByParentCommentIsNull()).thenReturn(10L);
        when(commentRepository.countByParentCommentIsNotNull()).thenReturn(4L);

        SuccessResponseDTO<Double> result = statsService.calculateAnsweredCommentsPercentage();

        assertEquals(40.0, result.data());
    }

    @Test
    void calculateAnsweredCommentsPercentage_WhenNoComments_ShouldReturnZero() {
        when(commentRepository.countByParentCommentIsNull()).thenReturn(0L);

        SuccessResponseDTO<Double> result = statsService.calculateAnsweredCommentsPercentage();

        assertEquals(0.0, result.data());
        assertEquals(ResponseMessages.NO_Reply_COMMENTS, result.errors());
    }

    @Test
    void countAnimalsBySpecies_WhenSpeciesExist_ShouldReturnCounts() {
        Species species1 = new Species();
        species1.setId(1L);
        species1.setName("Lion");
        Species species2 = new Species();
        species2.setId(2L);
        species2.setName("Tiger");

        when(speciesRepository.findAll()).thenReturn(Arrays.asList(species1, species2));
        when(animalRepository.countBySpecies(species1)).thenReturn(7L);
        when(animalRepository.countBySpecies(species2)).thenReturn(2L);

        SuccessResponseDTO<List<AnimalCountBySpecies>> result = statsService.countAnimalsBySpecies();

        assertEquals(2, result.data().size());
        assertEquals("Lion", result.data().get(0).speciesName());
        assertEquals(7L, result.data().get(0).animalCount());
    }

    @Test
    void getAnimalsByRegistrationDate_WhenDateValid_ShouldReturnAnimals() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Animal animal = new Animal(1L, "Leo", new Species(), new Zone(), new Date(),  new ArrayList<>());

        when(animalRepository.findByRegistrationDateBetween(any(), any()))
                .thenReturn(Collections.singletonList(animal));

        SuccessResponseDTO<List<AnimalResponse>> result = statsService.getAnimalsByRegistrationDate(date);

        assertNotNull(result.data());
        assertEquals(1, result.data().size());
    }

    @Test
    void searchAll_WhenQueryMatches_ShouldReturnResults() {
        String query = "lion";
        Species species = new Species();
        species.setId(1L);
        species.setName("Lion");
        when(zoneRepository.findByNameContainingIgnoreCase(query)).thenReturn(Collections.emptyList());
        when(speciesRepository.findByNameContainingIgnoreCase(query))
                .thenReturn(Collections.singletonList(species));
        when(animalRepository.findByNameContainingIgnoreCase(query)).thenReturn(Collections.emptyList());
        when(commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNull(query))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findByMessageContainingIgnoreCaseAndParentCommentIsNotNull(query))
                .thenReturn(Collections.emptyList());

        SuccessResponseDTO<SearchResults> result = statsService.searchAll(query);

        assertNotNull(result.data());
        assertEquals(1, result.data().species().size());
        assertEquals("Lion", result.data().species().get(0).name());
    }
}