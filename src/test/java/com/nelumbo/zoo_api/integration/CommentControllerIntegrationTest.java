package com.nelumbo.zoo_api.integration;

import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.models.*;
import com.nelumbo.zoo_api.repository.*;
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


import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class CommentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired
    private ZoneRepository zonaRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private SpeciesRepository speciesRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAll();
        zonaRepository.deleteAll();
        speciesRepository.deleteAll();
        commentRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    void addCommentToAnimal_ShouldReturn201() throws Exception {
        // Arrange
        // Crear zona, especie, animal y usuario autenticado
        Zone zone = zonaRepository.save(createZone("Savannah"));
        Species species = speciesRepository.save(createSpecies("León"));
        Animal animal = animalRepository.save(createAnimal("Lion", species, zone));
        userRepository.save(createUser("User", "user@example.com", "PASSword123!!", Role.EMPLOYEE));


        String requestBody = """
    {
        "animalId": %d,
        "message": "Great animal!"
    }
    """.formatted(animal.getId());

        // Act & Assert
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.message").value("Great animal!"))
                .andExpect(jsonPath("$.data.author").value("User")); // el nombre, no el email
    }

    private Comment createComment(String message, String author, Animal animal, Comment parent) {
        Comment reply = new Comment();
        reply.setMessage(message);
        reply.setAuthor(author);
        reply.setAnimal(animal);
        reply.setParentComment(parent);
        reply.setCreatedAt(new Date());
        return commentRepository.save(reply);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    void addReplyToComment_ShouldReturn201() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Selva"));
        Species species = speciesRepository.save(createSpecies("Felino"));
        Animal animal = animalRepository.save(createAnimal("Tigre", species, zone));

        userRepository.save(createUser("User", "user@example.com", "PASSword123!!", Role.EMPLOYEE));
        Comment parentComment = createComment("Nice!", "User", animal, null);
        commentRepository.save(parentComment);


        String requestBody = """
        {
            "message": "I agree!"
        }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/comments/%d/replies".formatted(parentComment.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parentCommentId").value(parentComment.getId()))
                .andExpect(jsonPath("$.data.message").value("I agree!"))
                .andExpect(jsonPath("$.data.author").value("User"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getCommentsForAnimal_ShouldReturn200() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Savannah"));
        Species species = speciesRepository.save(createSpecies("León"));
        Animal animal = animalRepository.save(createAnimal("León", species, zone));

        commentRepository.save(createComment("Nice!", "User", animal, null));

        // Act & Assert
        mockMvc.perform(get("/api/comments/zone/" + zone.getId() + "/animal/" + animal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].zoneName").value("Savannah"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "ROLE_ADMIN")
    void deleteComment_WhenAdmin_ShouldReturn204() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Montaña"));
        Species species = speciesRepository.save(createSpecies("Cóndor"));
        Animal animal = animalRepository.save(createAnimal("Cóndor", species, zone));
        userRepository.save(createUser("Admin", "admin@example.com", "PASSword123!!", Role.ADMIN));

        Comment comment = commentRepository.save(createComment("Comentario a borrar", "Admin", animal, null));

        // Act & Assert
        mockMvc.perform(delete("/api/comments/" + comment.getId()))
                .andExpect(status().isNoContent());

        assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    void deleteComment_WhenOwner_ShouldReturn204() throws Exception {
        // Arrange
        Zone zone = zonaRepository.save(createZone("Bosque"));
        Species species = speciesRepository.save(createSpecies("Zorro"));
        Animal animal = animalRepository.save(createAnimal("Zorro", species, zone));
        userRepository.save(createUser("User", "user@example.com", "PASSword123!!", Role.EMPLOYEE));

        Comment comment = createComment("Soy el dueño", "User", animal, null);
        comment = commentRepository.save(comment);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/" + comment.getId()))
                .andExpect(status().isNoContent());

        assertFalse(commentRepository.existsById(comment.getId()));
    }


    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    void getAllComments_ShouldReturn200() throws Exception {
        // Arrange
        User user = userRepository.save(createUser("User", "user@example.com", "PASSword123!!", Role.EMPLOYEE));
        Zone zone = zonaRepository.save(createZone("Bosque"));
        Species species = speciesRepository.save(createSpecies("Zorro"));
        Animal animal = animalRepository.save(createAnimal("Zorro", species, zone));
        commentRepository.save(createComment("Comment 1", user.getName(), animal, null));

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].message").value("Comment 1"))
                .andExpect(jsonPath("$.data[0].author").value("User"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllComments_WhenEmpty_ShouldReturnNoCommentsMessage() throws Exception {
        commentRepository.deleteAll();

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value(ResponseMessages.NO_COMMENTS));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    @Transactional
    void getCommentWithReplies_ShouldReturn200() throws Exception {
        // 1. Configurar datos de prueba
        User author = userRepository.save(createUser("User", "user@example.com", "PASSword123!!", Role.EMPLOYEE));
        Zone zone = zonaRepository.save(createZone("Bosque"));
        Species species = speciesRepository.save(createSpecies("Zorro"));
        Animal animal = animalRepository.save(createAnimal("Zorro", species, zone));

        // 2. Crear y guardar el comentario padre
        Comment parent = createComment("Main comment", author.getName(), animal, null);
        parent = commentRepository.save(parent);

        // 3. Crear y guardar la respuesta (reply)
        Comment reply = createComment("Reply", author.getName(), animal, parent);
        reply = commentRepository.save(reply);

        // 4. Actualizar la lista de replies del padre (importante para la relación bidireccional)
        parent.setReplies(List.of(reply));

        // 5. Ejecutar el test
        mockMvc.perform(get("/api/comments/{commentId}", parent.getId()))
                .andDo(print()) // Esto imprime la respuesta para depuración
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(parent.getId()))
                .andExpect(jsonPath("$.data.replies.length()").value(1))
                .andExpect(jsonPath("$.data.replies[0].id").value(reply.getId()))
                .andExpect(jsonPath("$.data.replies[0].parentCommentId").value(parent.getId()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void getCommentWithReplies_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/comments/{commentId}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].description").value("Comment not found"))
                .andExpect(jsonPath("$.errors[0].field").value("commentId"));
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_EMPLOYEE")
    void deleteComment_WhenNotOwnerNorAdmin_ShouldReturnForbidden() throws Exception {

        User autor = userRepository.save(createUser("User", "user2@example.com", "PASSword123!!", Role.EMPLOYEE));
        Zone zone = zonaRepository.save(createZone("Bosque"));
        Species species = speciesRepository.save(createSpecies("Zorro"));
        Animal animal = animalRepository.save(createAnimal("Zorro", species, zone));
        Comment comentario = createComment("Main comment", autor.getName(), animal, null);

        // Act & Assert
        // Intenta borrar el comentario con un usuario diferente (usuario_no_autor@test.com)
        mockMvc.perform(delete("/api/comments/" + comentario.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].description").value("No tienes permiso para borrar este comentario"));
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

    private User createUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }

}