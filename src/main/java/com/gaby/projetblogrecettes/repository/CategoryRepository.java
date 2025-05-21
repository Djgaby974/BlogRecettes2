package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.recipes WHERE c.id = :id")
    Optional<Category> findByIdWithRecipes(@Param("id") Long id);
    
    List<Category> findByNameContainingIgnoreCase(String keyword);
}