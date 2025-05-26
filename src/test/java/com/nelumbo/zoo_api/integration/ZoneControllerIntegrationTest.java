package com.nelumbo.zoo_api.integration;


import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_ADMIN")
@Transactional
@Rollback
class ZoneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ZoneRepository zonaRepository;
    @Autowired
    private AnimalRepository animalRepository;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAll();
        zonaRepository.deleteAll();
    }

    @Test
    void createZone_ShouldReturn201Created() throws Exception {
        String requestBody = """
        {
            "name": "Savannah"
        }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Savannah"));
    }

    @Test
    void getAllZones_ShouldReturn200Ok() throws Exception {
        // Arrange
        zonaRepository.save(createZone("Sierra"));
        zonaRepository.save(createZone("Jungle"));

        // Act & Assert
        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Sierra"))
                .andExpect(jsonPath("$.data[1].name").value("Jungle"));
    }

    @Test
    void getZoneById_WithExistingId_ShouldReturn200Ok() throws Exception {
        // Arrange: guardas la zona real en la base
        Zone saved = zonaRepository.save(createZone("Sierra"));

        // Act & Assert: llamas el endpoint real y verificas
        mockMvc.perform(get("/api/zones/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("Sierra"));
    }

    @Test
    void updateZone_ShouldReturn200Ok() throws Exception {
        // Arrange: guardas una zona real
        Zone saved = zonaRepository.save(createZone("Old Name"));

        String requestBody = """
    {
        "name": "Updated Name"
    }
    """;

        // Act & Assert: llamas al endpoint PUT y verificas respuesta
        mockMvc.perform(put("/api/zones/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));

        // Opcional: verifica que la zona se actualizó en la BD
        Zone updated = zonaRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void deleteZone_ShouldReturn204NoContent() throws Exception {
        // Arrange: Insertamos una zona
        Zone savedZone = zonaRepository.save(createZone("Sierra"));

        // Act & Assert: Llamamos al endpoint DELETE y esperamos 204
        mockMvc.perform(delete("/api/zones/" + savedZone.getId()))
                .andExpect(status().isNoContent());

        // Verificamos que ya no existe
        boolean exists = zonaRepository.existsById(savedZone.getId());
        assertFalse(exists, "La zona debería haber sido eliminada");
    }

    private Zone createZone(String name) {
        Zone zone = new Zone();
        zone.setName(name);
        return zonaRepository.save(zone);
    }

}