package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.CommentReplyRequest;
import com.nelumbo.zoo_api.dto.CommentRequest;
import com.nelumbo.zoo_api.dto.CommentResponse;
import com.nelumbo.zoo_api.dto.CommentResponse2;
import com.nelumbo.zoo_api.dto.errors.SuccessResponseDTO;
import com.nelumbo.zoo_api.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<SuccessResponseDTO<CommentResponse>> addCommentToAnimal(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addCommentToAnimal(request, userDetails.getUsername()));
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<SuccessResponseDTO<CommentResponse>> addReplyToComment(
            @PathVariable Long commentId,
            @Valid  @RequestBody CommentReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addReplyToComment(commentId, request, userDetails.getUsername()));
    }

//    @GetMapping("/animal/{animalId}")
//    public ResponseEntity<SuccessResponseDTO<List<CommentResponse2>>> getCommentsForAnimal(
//            @PathVariable Long animalId) {
//        return ResponseEntity.ok(commentService.getCommentsForAnimal(animalId));
//    }
    @GetMapping("/zone/{zoneId}/animal/{animalId}")
    public ResponseEntity<SuccessResponseDTO<List<CommentResponse2>>> getCommentsForAnimal(
            @PathVariable Long zoneId,
            @PathVariable Long animalId) {
        return ResponseEntity.ok(commentService.getCommentsForAnimal(zoneId, animalId));
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDTO<List<CommentResponse>>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<SuccessResponseDTO<CommentResponse>> getCommentWithReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentWithReplies(commentId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Verifica si el usuario es ADMIN (asumiendo que tienes un método para esto)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        commentService.deleteComment(commentId, userDetails.getUsername(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}