package com.nelumbo.zoo_api.integration;



import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.models.Species;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.SpeciesRepository;
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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_ADMIN")
@Transactional
@Rollback
class SpeciesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpeciesRepository speciesRepository;
    @Autowired
    private AnimalRepository animalRepository;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAll();
        speciesRepository.deleteAll();
    }

    @Test
    void createSpecies_ShouldReturn201Created() throws Exception {
        String requestBody = """
        {
            "name": "Reptiles"
        }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/species")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Reptiles"));
    }

    @Test
    void getAllSpecies_ShouldReturn200Ok() throws Exception {
        // Arrange

        Species species = createSpecies("Reptiles");
        speciesRepository.save(species);
        Species species2 = createSpecies("Anfibios");
        speciesRepository.save(species2);

        // Act & Assert
        mockMvc.perform(get("/api/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Reptiles"))
                .andExpect(jsonPath("$.data[1].name").value("Anfibios"));
    }
    private Species createSpecies(String name) {
        Species species = new Species();
        species.setName(name);
        return speciesRepository.save(species);
    }

    @Test
    void getAllSpecies_WhenEmpty_ShouldReturn200OkWithMessage() throws Exception {
        // Arrange - No añadimos ninguna especie

        // Act & Assert
        mockMvc.perform(get("/api/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(ResponseMessages.NO_SPECIES));
    }

    @Test
    void getSpeciesById_WithExistingId_ShouldReturn200Ok() throws Exception {
        // Arrange: guardas la Species real en la base
        Species species = createSpecies("Reptiles");
        Species saved = speciesRepository.save(species);

        // Act & Assert: llamas el endpoint real y verificas
        mockMvc.perform(get("/api/species/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("Reptiles"));
    }

    @Test
    void getSpeciesById_WithNonExistingId_ShouldReturn404() throws Exception {
        // Arrange
        long nonExistingId = 9999L;

        // Act & Assert
        mockMvc.perform(get("/api/species/" + nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].description").value("Especie no encontrada"))
                .andExpect(jsonPath("$.errors[0].field").value("id"));
    }



    @Test
    void updateSpecies_ShouldReturn200Ok() throws Exception {
        // Arrange: guardas una Species real
        Species species = createSpecies("Old Name");
        Species saved = speciesRepository.save(species);

        String requestBody = """
    {
        "name": "Updated Name"
    }
    """;

        // Act & Assert: llamas al endpoint PUT y verificas respuesta
        mockMvc.perform(put("/api/species/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));

        // Opcional: verifica que la Species se actualizó en la BD
        Species updated = speciesRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void deleteSpecies_ShouldReturn204NoContent() throws Exception {
        // Arrange: Insertamos una Species
        Species species = createSpecies("Reptiles");
        Species savedSpecies = speciesRepository.save(species);

        // Act & Assert: Llamamos al endpoint DELETE y esperamos 204
        mockMvc.perform(delete("/api/species/" + savedSpecies.getId()))
                .andExpect(status().isNoContent());

        // Verificamos que ya no existe
        boolean exists = speciesRepository.existsById(savedSpecies.getId());
        assertFalse(exists, "La especie debería haber sido eliminada");
    }
}