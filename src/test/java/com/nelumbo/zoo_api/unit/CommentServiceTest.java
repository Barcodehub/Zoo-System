package com.nelumbo.zoo_api.unit;

import com.nelumbo.zoo_api.dto.CommentReplyRequest;
import com.nelumbo.zoo_api.dto.CommentRequest;
import com.nelumbo.zoo_api.dto.CommentResponse;
import com.nelumbo.zoo_api.dto.CommentResponse2;
import com.nelumbo.zoo_api.dto.errors.ResponseMessages;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Comment;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.models.Zone;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.UserRepository;
import com.nelumbo.zoo_api.repository.ZoneRepository;
import com.nelumbo.zoo_api.services.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private UserRepository userRepository;
    @Mock private ZoneRepository zoneRepository;

    @InjectMocks private CommentService commentService;

    @Test
    void addCommentToAnimal_WhenValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        CommentRequest request = new CommentRequest(1L, "Great animal!");
        User user = new User();
        user.setName("John Doe");
        Animal animal = new Animal(1L, "Lion", null, null, new Date(), null);

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        // Act
        SuccessResponseDTO<CommentResponse> response =
                commentService.addCommentToAnimal(request, "john@example.com");

        // Assert
        assertNotNull(response);
        assertNull(response.errors());
        assertEquals(1L, response.data().id());
        assertEquals("Great animal!", response.data().message());
        assertEquals("John Doe", response.data().author());
        assertEquals(1L, response.data().animalId());
        assertNull(response.data().parentCommentId());

        verify(animalRepository).findById(1L);
        verify(userRepository).findByEmail("john@example.com");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addReplyToComment_WhenValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        Long parentId = 1L;
        CommentReplyRequest request = new CommentReplyRequest("Nice comment!");
        User user = new User();
        user.setName("Jane Smith");
        Comment parentComment = new Comment();
        parentComment.setId(parentId);
        parentComment.setAnimal(new Animal(2L, "Tiger", null, null, new Date(), null));

        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        // Act
        SuccessResponseDTO<CommentResponse> response =
                commentService.addReplyToComment(parentId, request, "jane@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.data().id());
        assertEquals(parentId, response.data().parentCommentId());
        assertEquals("Jane Smith", response.data().author());

        verify(commentRepository).findById(parentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteComment_WhenAdmin_ShouldDeleteRegardlessOfOwner() {
        // Arrange
        Long commentId = 1L;
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setAuthor("Original Author");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findEmailByFullName("Original Author"))
                .thenReturn(Optional.of("original@example.com"));

        // Act
        commentService.deleteComment(commentId, "admin@example.com", true);

        // Assert
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_WhenNotOwnerNorAdmin_ShouldThrowAccessDenied() {
        // Arrange
        Long commentId = 1L;
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setAuthor("Original Author");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findEmailByFullName("Original Author"))
                .thenReturn(Optional.of("original@example.com"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                commentService.deleteComment(commentId, "other@example.com", false));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void getCommentsForAnimal_WhenNoComments_ShouldReturnNoCommentsMessage() {
        // Arrange
        Long zoneId = 1L;
        Long animalId = 1L;
        Zone zone = new Zone(zoneId, "Savannah", new ArrayList<>());
        Animal animal = new Animal(animalId, "Lion", null, zone, new Date(), null);

        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(animalRepository.findByIdAndZoneId(animalId, zoneId)).thenReturn(Optional.of(animal));
        when(commentRepository.findByAnimalIdAndParentCommentIsNullWithReplies(animalId))
                .thenReturn(new ArrayList<>());

        // Act
        SuccessResponseDTO<List<CommentResponse2>> response =
                commentService.getCommentsForAnimal(zoneId, animalId);

        // Assert
        assertNotNull(response);
        assertNull(response.data());
        assertEquals(ResponseMessages.NO_COMMENTS_FOR_ANIMAL, response.errors());
    }

    @Test
    void getCommentsForAnimal_WhenZoneNotFound_ShouldThrowException() {
        // Arrange
        Long zoneId = 1L;
        Long animalId = 1L;

        when(zoneRepository.findById(zoneId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                commentService.getCommentsForAnimal(zoneId, animalId));

        assertEquals("Zona no encontrada", exception.getMessage());
        assertEquals("zoneId", exception.getField());
    }

    @Test
    void getCommentsForAnimal_WhenAnimalNotInZone_ShouldThrowException() {
        // Arrange
        Long zoneId = 1L;
        Long animalId = 1L;
        Zone zone = new Zone(zoneId, "Savannah", new ArrayList<>());

        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(animalRepository.findByIdAndZoneId(animalId, zoneId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                commentService.getCommentsForAnimal(zoneId, animalId));

        assertEquals("Animal no encontrado en la zona especificada", exception.getMessage());
        assertEquals("animalId", exception.getField());
    }

    @Test
    void getCommentsForAnimal_WithComments_ShouldReturnStructuredResponse() {
        // Arrange
        Long zoneId = 1L;
        Long animalId = 1L;
        Zone zone = new Zone(zoneId, "Savannah", new ArrayList<>());
        Animal animal = new Animal(animalId, "Lion", null, zone, new Date(), null);

        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setMessage("Parent");
        parentComment.setAnimal(animal);

        Comment reply = new Comment();
        reply.setId(2L);
        reply.setMessage("Reply");
        reply.setParentComment(parentComment);
        reply.setAnimal(animal);

        parentComment.setReplies(List.of(reply));

        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(animalRepository.findByIdAndZoneId(animalId, zoneId)).thenReturn(Optional.of(animal));
        when(commentRepository.findByAnimalIdAndParentCommentIsNullWithReplies(animalId))
                .thenReturn(List.of(parentComment));

        // Act
        SuccessResponseDTO<List<CommentResponse2>> response =
                commentService.getCommentsForAnimal(zoneId, animalId);

        // Assert
        assertNotNull(response);
        assertNull(response.errors());
        assertEquals(1, response.data().size());
        assertEquals(1, response.data().get(0).replies().size());
        assertEquals("Lion", response.data().get(0).animalName());
        assertEquals("Savannah", response.data().get(0).zoneName());
    }



    @Test
    void getAllComments_WhenCommentsExist_ShouldReturnList() {
        // Arrange
        Comment comment1 = new Comment(1L, "Comment 1", "User1", new Date(),
                new Animal(), null, new ArrayList<>());
        Comment comment2 = new Comment(2L, "Comment 2", "User2", new Date(),
                new Animal(), null, new ArrayList<>());

        when(commentRepository.findAll()).thenReturn(List.of(comment1, comment2));

        // Act
        SuccessResponseDTO<List<CommentResponse>> response = commentService.getAllComments();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.data().size());
        assertEquals("Comment 1", response.data().get(0).message());
        verify(commentRepository).findAll();
    }

    @Test
    void getAllComments_WhenNoComments_ShouldReturnNoCommentsMessage() {
        // Arrange
        when(commentRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        SuccessResponseDTO<List<CommentResponse>> response = commentService.getAllComments();

        // Assert
        assertNotNull(response);
        assertNull(response.data());
        assertEquals(ResponseMessages.NO_COMMENTS, response.errors());
    }

    @Test
    void getCommentWithReplies_WhenCommentExists_ShouldReturnComment() {
        // Arrange
        Long commentId = 1L;
        Comment comment = new Comment(commentId, "Main comment", "Author", new Date(),
                new Animal(), null, new ArrayList<>());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act
        SuccessResponseDTO<CommentResponse> response = commentService.getCommentWithReplies(commentId);

        // Assert
        assertNotNull(response);
        assertEquals(commentId, response.data().id());
        assertEquals("Main comment", response.data().message());
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentWithReplies_WhenCommentNotFound_ShouldThrowException() {
        // Arrange
        Long commentId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentWithReplies(commentId);
        });
        verify(commentRepository).findById(commentId);
    }
}