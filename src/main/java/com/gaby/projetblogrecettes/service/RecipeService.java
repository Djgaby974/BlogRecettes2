package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.CategoryRepository;
import com.gaby.projetblogrecettes.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository,
                         UserService userService,
                         CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Recipe createRecipe(Recipe recipe, String username) {
        User author = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        recipe.setAuthor(author);
        recipe.setCreatedAt(LocalDateTime.now());
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(Long id, Recipe recipeDetails) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recette non trouvée"));

        recipe.setTitle(recipeDetails.getTitle());
        recipe.setDescription(recipeDetails.getDescription());
        recipe.setInstructions(recipeDetails.getInstructions());
        recipe.setIngredients(recipeDetails.getIngredients());
        recipe.setPrepTime(recipeDetails.getPrepTime());
        recipe.setCookTime(recipeDetails.getCookTime());
        recipe.setServingSize(recipeDetails.getServingSize());
        recipe.setDifficulty(recipeDetails.getDifficulty());

        return recipeRepository.save(recipe);
    }

    public Page<Recipe> searchRecipes(String keyword, Long userId, Difficulty difficulty, Pageable pageable) {
        return recipeRepository.searchRecipes(keyword, userId, difficulty, pageable);
    }


    public List<Recipe> getRecentRecipes() {
        return recipeRepository.findTop10ByPublishedTrueOrderByCreatedAtDesc();
    }

    public void deleteRecipe(Long id, String username) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recette non trouvée avec l'id: " + id));

        if (!recipe.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette recette");
        }

        recipeRepository.deleteById(id);
        log.info("Recette {} supprimée par {}", id, username);
    }

    public Difficulty parseDifficulty(String difficultyStr) {
        if (difficultyStr == null || difficultyStr.isEmpty()) return null;
        try {
            return Difficulty.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Page<Recipe> searchRecipes(String keyword, Long categoryId, String difficulty, boolean published, Pageable pageable) {
        Difficulty difficultyEnum = parseDifficulty(difficulty);
        return recipeRepository.findAll(pageable);
    }

    public Page<Recipe> getRecipesByAuthor(String username, Pageable pageable) {
        User author = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return recipeRepository.findByAuthor(author, pageable);
    }
}
