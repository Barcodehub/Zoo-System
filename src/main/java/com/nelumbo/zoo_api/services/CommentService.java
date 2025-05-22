package com.nelumbo.zoo_api.services;


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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class CommentService {
    private final CommentRepository commentRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;


    public SuccessResponseDTO<CommentResponse> addCommentToAnimal(@Valid CommentRequest request, String userEmail) {

        Animal animal = getAnimal(request.animalId());
        User user = getUser(userEmail);

        Comment comment = new Comment();
        comment.setMessage(request.message());
        comment.setAuthor(user.getName());
        comment.setAnimal(animal);
        comment.setCreatedAt(new Date());

        comment = commentRepository.save(comment);
        return new SuccessResponseDTO<>(mapToCommentResponse(comment));
    }

    public SuccessResponseDTO<CommentResponse> addReplyToComment(Long commentId, @Valid CommentReplyRequest request, String userEmail) {

        Comment parentComment = getCommentOrThrow(commentId);
        User user = getUser(userEmail);

        Comment reply = new Comment();
        reply.setMessage(request.message());
        reply.setAuthor(user.getName());
        reply.setAnimal(parentComment.getAnimal());
        reply.setParentComment(parentComment);
        reply.setCreatedAt(new Date());

        reply = commentRepository.save(reply);
        return new SuccessResponseDTO<>( mapToCommentResponse(reply));
    }

    public SuccessResponseDTO<List<CommentResponse2>> getCommentsForAnimal(Long zoneId, Long animalId) {

        Zone zone = validateZoneExists(zoneId);
        Animal animal = validateAnimalExistsInZone(animalId, zoneId);

        // Obtener comentarios principales (sin parent) con sus respuestas en una sola consulta
        List<Comment> topLevelComments = commentRepository.findByAnimalIdAndParentCommentIsNullWithReplies(animalId);

        // Mapear a DTO con estructura jerárquica
        List<CommentResponse2> comments = topLevelComments.stream()
                .map(comment -> mapToCommentResponseWithReplies(comment, zone, animal))
                .toList();

        // Retornar respuesta
        return comments.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_COMMENTS_FOR_ANIMAL)
                : new SuccessResponseDTO<>(comments);
    }


    public SuccessResponseDTO<List<CommentResponse>> getAllComments() {
        List<CommentResponse> result = commentRepository.findAll().stream()
                .map(this::mapToCommentResponse)
                .toList();
        return result.isEmpty()
                ? new SuccessResponseDTO<>(null, ResponseMessages.NO_COMMENTS)
                : new SuccessResponseDTO<>(result);
    }

    public SuccessResponseDTO<CommentResponse> getCommentWithReplies(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);

        return new SuccessResponseDTO<>( mapToCommentResponse(comment));
    }

    public void deleteComment(Long commentId, String currentUserEmail, boolean isAdmin) {
        Comment comment = getCommentOrThrow(commentId);

        // Solo permite borrar si es el autor o admin
        String authorEmail = userRepository.findEmailByFullName(comment.getAuthor())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + comment.getAuthor(), null));

        if (!isAdmin && !authorEmail.equals(currentUserEmail)) {
            throw new AccessDeniedException("No tienes permiso para borrar este comentario");
        }


        commentRepository.delete(comment);
    }



    private User getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", null));

    }

    private Animal getAnimal(Long animalId) {
        return animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "animalId"));

    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found", "commentId"));
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies() != null ?
                comment.getReplies().stream()
                        .map(this::mapToCommentResponse)
                        .collect(Collectors.toList()) :
                List.of();

        return new CommentResponse(
                comment.getId(),
                comment.getMessage(),
                comment.getAuthor(),
                comment.getCreatedAt().toString(),
                comment.getAnimal().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                replies
        );
    }

    private CommentResponse2 mapToCommentResponseWithReplies(Comment comment, Zone zone, Animal animal) {
        return new CommentResponse2(
                comment.getId(),
                comment.getMessage(),
                comment.getAuthor(),
                formatDate(comment.getCreatedAt()),
                animal.getId(),
                animal.getName(),
                zone.getId(),
                zone.getName(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getReplies().stream()
                        .map(reply -> mapToCommentResponseWithReplies(reply, zone, animal))
                        .toList()
        );
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private Zone validateZoneExists(Long zoneId) {
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada", "zoneId"));
    }

    private Animal validateAnimalExistsInZone(Long animalId, Long zoneId) {
        return animalRepository.findByIdAndZoneId(animalId, zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal no encontrado en la zona especificada",
                        "animalId"));
    }

}