package com.nelumbo.zoo_api.integration;

import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_ADMIN")
@Transactional
@Rollback
class AnimalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ZoneRepository zonaRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private SpeciesRepository speciesRepository;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAll();
        zonaRepository.deleteAll();
        speciesRepository.deleteAll();
    }

    @Test
    void createAnimal_ShouldReturn201Created() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Sabana"));
        Species species = speciesRepository.save(createSpecies("Felino"));

        String requestBody = String.format("""
    {
        "name": "León",
        "speciesId": %d,
        "zoneId": %d
    }
    """, species.getId(), zone.getId());

        // Act & Assert
        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("León"))
                .andExpect(jsonPath("$.data.species.name").value("Felino"));
    }

    @Test
    void createAnimalwithoutSpecieANDZone_ShouldReturn201Created() throws Exception {


        String requestBody = """
    {
        "name": "Leon"
    }
    """;

        // Act & Assert
        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Leon"));
    }

    @Test
    void getAllAnimals_WhenAnimals_ShouldReturn200() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Sabana"));
        Species species = speciesRepository.save(createSpecies("Felino"));
        animalRepository.save(createAnimal("Tigre Bengalí", species, zone));

        // Act & Assert
        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Tigre Bengalí"))
                .andExpect(jsonPath("$.data[0].species.name").value("Felino"))
                .andExpect(jsonPath("$.data[0].zone.name").value("Sabana"));
    }

    @Test
    void getAllAnimals_WhenNoAnimals_ShouldReturnEmptyList() throws Exception {
        // Arrange
        animalRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value(ResponseMessages.NO_ANIMALS));
    }


    @Test
    void updateAnimal_ShouldReturnUpdatedAnimal() throws Exception {
        // Arrange: crear zona y especie necesarias
        Zone initialZone = zonaRepository.save(createZone("Old Zone"));
        Zone updatedZone = zonaRepository.save(createZone("Nueva Zona"));

        Species species = speciesRepository.save(createSpecies("León"));

        Animal animal = animalRepository.save(createAnimal(
                "León Viejo",
                species,
                initialZone
        ));

        String requestBody = String.format("""
        {
            "name": "León Actualizado",
            "speciesId": %d,
            "zoneId": %d
        }
        """, species.getId(), updatedZone.getId());

        // Act & Assert
        mockMvc.perform(put("/api/animals/" + animal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("León Actualizado"))
                .andExpect(jsonPath("$.data.zone.name").value("Nueva Zona"));
    }


    @Test
    void deleteAnimal_ShouldReturn204NoContent() throws Exception {
        // Arrange: crear zona y especie necesarias para el animal
        Zone zone = zonaRepository.save(createZone("Selva"));
        Species species = speciesRepository.save(createSpecies("Tigre"));

        // Crear y guardar el animal
        Animal animal = animalRepository.save(createAnimal("Tigre Bengalí", species, zone));

        // Act: eliminar el animal
        mockMvc.perform(delete("/api/animals/" + animal.getId()))
                .andExpect(status().isNoContent());

        // Assert: verificar que fue eliminado
        boolean exists = animalRepository.existsById(animal.getId());
        assertFalse(exists, "El animal debería haber sido eliminado");
    }

    @Test
    void getAnimalById_WhenAnimalExists_ShouldReturnAnimal() throws Exception {
        // Arrange
        Species species = speciesRepository.save(createSpecies("Felino"));
        Zone zone = zonaRepository.save(createZone("Sabana"));

        Animal animal = animalRepository.save(createAnimal("León", species, zone));

        // Act & Assert
        mockMvc.perform(get("/api/animals/" + animal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(animal.getId()))
                .andExpect(jsonPath("$.data.name").value("León"))
                .andExpect(jsonPath("$.data.species.name").value("Felino"))
                .andExpect(jsonPath("$.data.zone.name").value("Sabana"));
    }
    @Test
    void getAnimalById_WhenAnimalNotExists_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/animals/9999")) // Un ID que no existe
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].description").value("Animal no encontrado"))
                .andExpect(jsonPath("$.errors[0].field").value("id"));
    }



    @Test
    void createAnimal_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {

        String requestBody = """
    {
        "name": "",
        "speciesId": null,
        "zoneId": null
    }
    """;

        // Act & Assert
        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].description").value("must not be blank"))
                .andExpect(jsonPath("$.errors[?(@.field == 'speciesId')]").doesNotExist()) // Verifica que no hay error para speciesId
                .andExpect(jsonPath("$.errors[?(@.field == 'zoneId')]").doesNotExist()); // Verifica que no hay error para zoneId
    }

    @Test
    void updateAnimal_WhenAnimalNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Sabana"));
        Species species = speciesRepository.save(createSpecies("Felino"));

        String requestBody = String.format("""
    {
        "name": "León Actualizado",
        "speciesId": %d,
        "zoneId": %d
    }
    """, species.getId(), zone.getId());

        // Act & Assert - intentar actualizar un ID que no existe
        mockMvc.perform(put("/api/animals/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].description").value("Animal no encontrado"))
                .andExpect(jsonPath("$.errors[0].field").value("id"));
    }


    private Zone createZone(String name) {
        Zone zone = new Zone();
        zone.setName(name);
        return zonaRepository.save(zone);
    }

    private Species createSpecies(String name) {
        Species species = new Species();
        species.setName(name);
        return speciesRepository.save(species);
    }

    private Animal createAnimal(String name, Species species, Zone zone) {
        Animal animal = new Animal();
        animal.setName(name);
        animal.setSpecies(species);
        animal.setZone(zone);
        return animalRepository.save(animal);
    }


}