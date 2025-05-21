package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.Rating;
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
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndRecipe(User user, Recipe recipe);
    List<Rating> findByRecipe(Recipe recipe);
    
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.recipe.id = :recipeId")
    Optional<Double> calculateAverageRating(@Param("recipeId") Long recipeId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.recipe.id = :recipeId")
    Long countByRecipeId(@Param("recipeId") Long recipeId);
    
    // Trouver les meilleures recettes basées sur la moyenne des notes
    @Query("SELECT r.recipe, AVG(r.value) as avgRating " +
           "FROM Rating r " +
           "GROUP BY r.recipe " +
           "ORDER BY avgRating DESC")
    Page<Object[]> findTopRatedRecipes(Pageable pageable);
}