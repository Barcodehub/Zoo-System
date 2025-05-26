package com.nelumbo.zoo_api.integration;

import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Comment;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.nelumbo.zoo_api.dto.errors.ResponseMessages.NO_RESULTS_SEARCH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_ADMIN")
@Transactional
@Rollback
class StatsControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AnimalRepository animalRepository;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private SpeciesRepository speciesRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        animalRepository.deleteAll();
        speciesRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @Test
    void getAnimalsByZone_ShouldReturn200() throws Exception {
        Zone zone = new Zone();
        zone.setName("Desierto");
        zone = zoneRepository.save(zone);

        Species species = new Species();
        species.setName("Lion");
        species = speciesRepository.save(species);

        Animal animal1 = new Animal();
        animal1.setName("Leo");
        animal1.setSpecies(species);
        animal1.setZone(zone);
        animal1.setRegistrationDate(new Date());
        animalRepository.save(animal1);

        Animal animal2 = new Animal();
        animal2.setName("Simba");
        animal2.setSpecies(species);
        animal2.setZone(zone);
        animal2.setRegistrationDate(new Date());
        animalRepository.save(animal2);


        mockMvc.perform(get("/api/stats/animals-by-zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].zoneName").value("Desierto"))
                .andExpect(jsonPath("$.data[0].animalCount").value(2));
    }

    @Test
    void getAnsweredCommentsPercentage_ShouldReturn200() throws Exception {
        Zone zone = new Zone();
        zone.setName("Desierto");
        zone = zoneRepository.save(zone);

        Species species = new Species();
        species.setName("Lion");
        species = speciesRepository.save(species);

        Animal animal1 = new Animal();
        animal1.setName("Leo");
        animal1.setSpecies(species);
        animal1.setZone(zone);
        animal1.setRegistrationDate(new Date());
        animalRepository.save(animal1);

        Comment originalComment = new Comment();
        originalComment.setMessage("Original");
        originalComment.setAuthor("Admin");
        originalComment.setAnimal(animal1);
        originalComment.setParentComment(null);
        originalComment.setCreatedAt(new Date());
        originalComment = commentRepository.save(originalComment);

        Comment replyComment = new Comment();
        replyComment.setMessage("Reply");
        replyComment.setAuthor("User");
        replyComment.setAnimal(animal1);
        replyComment.setParentComment(originalComment);
        replyComment.setCreatedAt(new Date());
        commentRepository.save(replyComment);

        mockMvc.perform(get("/api/stats/answered-comments-percentage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void getAnimalsBySpecies_ShouldReturn200() throws Exception {
        Zone zone = new Zone();
        zone.setName("Desierto");
        zone = zoneRepository.save(zone);

        Species species = new Species();
        species.setName("Lion");
        species = speciesRepository.save(species);

        Animal animal1 = new Animal();
        animal1.setName("Leo");
        animal1.setSpecies(species);
        animal1.setZone(zone);
        animal1.setRegistrationDate(new Date());
        animalRepository.save(animal1);

        mockMvc.perform(get("/api/stats/animals-by-species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].speciesName").value("Lion"))
                .andExpect(jsonPath("$.data[0].animalCount").value(1));
    }

    @Test
    void getAnimalsByDate_ShouldReturn200() throws Exception {
        Zone zone = new Zone();
        zone.setName("Desierto");
        zone = zoneRepository.save(zone);

        Species species = new Species();
        species.setName("Lion");
        species = speciesRepository.save(species);

        Animal animal = new Animal();
        animal.setName("Leo");
        animal.setSpecies(species);
        animal.setZone(zone);
        animal.setRegistrationDate(Date.from(LocalDate.of(2023, 1, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()));

        animalRepository.save(animal);

        mockMvc.perform(get("/api/stats/animals-by-date").param("date", "2023-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Leo"));
    }

    @Test
    void search_ShouldReturn200() throws Exception {
        Species species = new Species();
        species.setName("Lion");
        speciesRepository.save(species);

        mockMvc.perform(get("/api/stats/search").param("query", "lion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.species[0].name").value("Lion"));
    }

    @Test
    void searchempty_ShouldReturn200() throws Exception {

        mockMvc.perform(get("/api/stats/search").param("query", "lion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value(NO_RESULTS_SEARCH +"lion"));
    }


}
