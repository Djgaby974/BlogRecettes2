package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.Category;
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

    List<Recipe> findByPublishedTrue();

    Page<Recipe> findByAuthor(User author, Pageable pageable);

    List<Recipe> findByCategory(Category category);

    List<Recipe> findByTitleContainingIgnoreCase(String keyword);



    @Query("""
    SELECT r FROM Recipe r
    WHERE (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:categoryId IS NULL OR r.category.id = :categoryId)
    AND (:difficulty IS NULL OR r.difficulty = :difficulty)
""")
    Page<Recipe> searchRecipes(
            @Param("title") String title,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable
    );



    List<Recipe> findTop10ByPublishedTrueOrderByCreatedAtDesc();

    List<Recipe> findByPrepTimeLessThanEqual(Integer maxPrepTime);


}
