package com.nelumbo.zoo_api.services;

import com.nelumbo.zoo_api.dto.CommentReplyRequest;
import com.nelumbo.zoo_api.dto.CommentRequest;
import com.nelumbo.zoo_api.dto.CommentResponse;
import com.nelumbo.zoo_api.exception.ResourceNotFoundException;
import com.nelumbo.zoo_api.models.Animal;
import com.nelumbo.zoo_api.models.Comment;
import com.nelumbo.zoo_api.models.User;
import com.nelumbo.zoo_api.repository.AnimalRepository;
import com.nelumbo.zoo_api.repository.CommentRepository;
import com.nelumbo.zoo_api.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    public CommentResponse addCommentToAnimal(@Valid CommentRequest request, String userEmail) {
        Animal animal = animalRepository.findById(request.animalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "animalId"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", null));

        Comment comment = new Comment();
        comment.setMessage(request.message());
        comment.setAuthor(user.getName());
        comment.setAnimal(animal);
        comment.setCreatedAt(new Date());

        comment = commentRepository.save(comment);
        return mapToCommentResponse(comment);
    }

    public CommentResponse addReplyToComment(Long commentId, CommentReplyRequest request, String userEmail) {

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found", "commentId"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", null));

        Comment reply = new Comment();
        reply.setMessage(request.message());
        reply.setAuthor(user.getName());
        reply.setAnimal(parentComment.getAnimal());
        reply.setParentComment(parentComment);
        reply.setCreatedAt(new Date());

        reply = commentRepository.save(reply);
        return mapToCommentResponse(reply);
    }

    public List<CommentResponse> getCommentsForAnimal(
            @PathVariable Long animalId) {
        animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found", "animalId"));

        return commentRepository.findByAnimalIdAndParentCommentIsNull(animalId).stream()
                .map(this::mapToCommentResponse)
                .toList();
    }

    public CommentResponse getCommentWithReplies(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found", "commentId"));

        return mapToCommentResponse(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found", "commentId"));

        commentRepository.delete(comment);
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
}