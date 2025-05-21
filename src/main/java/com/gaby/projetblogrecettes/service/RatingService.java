package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.Rating;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.RatingRepository;
import com.gaby.projetblogrecettes.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final UserService userService;

    @Autowired
    public RatingService(RatingRepository ratingRepository,
                        RecipeRepository recipeRepository,
                        UserService userService) {
        this.ratingRepository = ratingRepository;
        this.recipeRepository = recipeRepository;
        this.userService = userService;
    }

    public Rating rateRecipe(Long recipeId, String username, int value) {
        User user = userService.getUserByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RuntimeException("Recette non trouvée"));

        Rating rating = ratingRepository.findByUserAndRecipe(user, recipe)
            .orElse(new Rating());

        rating.setUser(user);
        rating.setRecipe(recipe);
        rating.setValue(value);

        return ratingRepository.save(rating);
    }

    public Double getAverageRating(Long recipeId) {
        return ratingRepository.calculateAverageRating(recipeId)
            .orElse(0.0);
    }

    public List<Recipe> getTopRatedRecipes(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return ratingRepository.findTopRatedRecipes(pageRequest)
            .getContent()
            .stream()
            .map(result -> (Recipe) result[0])
            .collect(Collectors.toList());
    }
}