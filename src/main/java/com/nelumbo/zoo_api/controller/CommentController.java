package com.nelumbo.zoo_api.controller;

import com.nelumbo.zoo_api.dto.CommentReplyRequest;
import com.nelumbo.zoo_api.dto.CommentRequest;
import com.nelumbo.zoo_api.dto.CommentResponse;
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
    public ResponseEntity<CommentResponse> addCommentToAnimal(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addCommentToAnimal(request, userDetails.getUsername()));
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentResponse> addReplyToComment(
            @PathVariable Long commentId,
            @RequestBody CommentReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addReplyToComment(commentId, request, userDetails.getUsername()));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<CommentResponse>> getCommentsForAnimal(
            @PathVariable Long animalId) {
        return ResponseEntity.ok(commentService.getCommentsForAnimal(animalId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentWithReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentWithReplies(commentId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}