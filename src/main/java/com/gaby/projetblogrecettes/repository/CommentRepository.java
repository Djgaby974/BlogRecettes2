package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.Comment;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipe(Recipe recipe);
    List<Comment> findByUser(User user);
    
    @Query("SELECT c FROM Comment c WHERE c.recipe.id = :recipeId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByRecipeIdAndParentIsNullOrderByCreatedAtDesc(
            @Param("recipeId") Long recipeId, 
            Pageable pageable);
            
    @Query("SELECT c FROM Comment c WHERE c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByParentIsNullOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.recipe.id = :recipeId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findWithRepliesByRecipeId(@Param("recipeId") Long recipeId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.id = :commentId")
    Optional<Comment> findByIdWithReplies(@Param("commentId") Long commentId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    List<Comment> findByRecipeIdWithUser(@Param("recipeId") Long recipeId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.recipe ORDER BY c.createdAt DESC")
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}