package com.nelumbo.zoo_api.repository;

import com.nelumbo.zoo_api.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnimalId(Long animalId);
    List<Comment> findByParentCommentId(Long parentCommentId);

        List<Comment> findByAnimalIdAndParentCommentIsNull(Long animalId);

        @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.id = :id")
        Optional<Comment> findByIdWithReplies(@Param("id") Long id);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.message) LIKE LOWER(CONCAT('%', :query, '%')) AND c.parentComment IS NULL")
    List<Comment> searchComments(@Param("query") String query);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.message) LIKE LOWER(CONCAT('%', :query, '%')) AND c.parentComment IS NOT NULL")
    List<Comment> searchReplies(@Param("query") String query);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment IS NULL")
    long countTotalComments();

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment IS NOT NULL")
    long countTotalReplies();

    @Query("SELECT c FROM Comment c WHERE c.animal.id = :animalId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByAnimalId(@Param("animalId") Long animalId);

    long countByParentCommentIsNull();
    long countByParentCommentIsNotNull();

    List<Comment> findByMessageContainingIgnoreCaseAndParentCommentIsNull(String message);
    List<Comment> findByMessageContainingIgnoreCaseAndParentCommentIsNotNull(String message);
}