package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.Rating;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.service.RatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@Slf4j
public class RatingController {
    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/recipes/{recipeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Rating> rateRecipe(@PathVariable Long recipeId,
                                           @RequestParam int value,
                                           Principal principal) {
        Rating rating = ratingService.rateRecipe(recipeId, principal.getName(), value);
        return new ResponseEntity<>(rating, HttpStatus.CREATED);
    }

    @GetMapping("/recipes/{recipeId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long recipeId) {
        Double average = ratingService.getAverageRating(recipeId);
        return ResponseEntity.ok(average);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Recipe>> getTopRatedRecipes(
            @RequestParam(defaultValue = "10") int limit) {
        List<Recipe> topRecipes = ratingService.getTopRatedRecipes(limit);
        return ResponseEntity.ok(topRecipes);
    }
}