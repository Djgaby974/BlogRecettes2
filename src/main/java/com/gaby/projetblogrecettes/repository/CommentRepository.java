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

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipe(Recipe recipe);
    List<Comment> findByUser(User user);
    
    @Query("SELECT c FROM Comment c WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    Page<Comment> findByRecipeIdOrderByCreatedAtDesc(
            @Param("recipeId") Long recipeId, 
            Pageable pageable
    );
    
    void deleteByRecipe(Recipe recipe);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.recipe.id = :recipeId")
    Long countByRecipeId(@Param("recipeId") Long recipeId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    List<Comment> findByRecipeIdWithUser(@Param("recipeId") Long recipeId);
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.recipe ORDER BY c.createdAt DESC")
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}