package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.service.CommentService;
import com.gaby.projetblogrecettes.service.RatingService;
import com.gaby.projetblogrecettes.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequestMapping("/api/recipes")
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;
    private final RatingService ratingService;
    private final CommentService commentService;

    @Autowired
    public RecipeController(RecipeService recipeService, 
                          RatingService ratingService,
                          CommentService commentService) {
        this.recipeService = recipeService;
        this.ratingService = ratingService;
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe, 
                                             Principal principal) {
        Recipe newRecipe = recipeService.createRecipe(recipe, principal.getName());
        return new ResponseEntity<>(newRecipe, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Recipe>> searchRecipes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "true") boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Recipe> recipes = recipeService.searchRecipes(keyword, categoryId, 
                                                         difficulty, published, pageable);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Recette non trouvée"));
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, 
                                             @RequestBody Recipe recipeDetails, 
                                             Principal principal) {
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id, 
                                           Principal principal) {
        recipeService.deleteRecipe(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}