package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.Category;
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
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // Recherche de recettes publiées
    List<Recipe> findByPublishedTrue();
    
    // Recherche par auteur
    List<Recipe> findByAuthor(User author);
    
    // Recherche par catégorie
    List<Recipe> findByCategory(Category category);
    
    // Recherche par titre contenant un mot-clé
    List<Recipe> findByTitleContainingIgnoreCase(String keyword);
    
    // Recherche avancée
    @Query("SELECT r FROM Recipe r WHERE " +
           "(:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR r.category.id = :categoryId) AND " +
           "(:difficulty IS NULL OR r.difficulty = :difficulty) AND " +
           "r.published = :published")
    Page<Recipe> searchRecipes(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") String difficulty,
            @Param("published") boolean published,
            Pageable pageable
    );
    
    // Recettes les plus récentes
    List<Recipe> findTop10ByPublishedTrueOrderByCreatedAtDesc();
    
    // Recherche par temps de préparation
    List<Recipe> findByPrepTimeLessThanEqual(Integer maxPrepTime);
}