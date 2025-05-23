package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.MealPlan;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final MealPlanRepository mealPlanRepository;
    
    /**
     * Récupère les ingrédients d'une recette spécifique
     */
    public Map<String, List<String>> getIngredientsByRecipe(List<MealPlan> mealPlans, Long recipeId) {
        // Trouver la recette dans les repas planifiés
        Optional<Recipe> recipeOpt = mealPlans.stream()
            .map(MealPlan::getRecipe)
            .filter(r -> r.getId().equals(recipeId))
            .findFirst();
            
        if (recipeOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Recipe recipe = recipeOpt.get();
        Map<String, List<String>> ingredientsByCategory = new TreeMap<>();
        
        // Catégorie par défaut pour les ingrédients
        final String DEFAULT_CATEGORY = "Ingrédients";
        
        // Vérifier si la liste des ingrédients n'est pas null
        if (recipe.getIngredients() != null) {
            // Créer une nouvelle liste pour éviter les problèmes de modification
            List<String> ingredients = new ArrayList<>(recipe.getIngredients());
            ingredientsByCategory.put(DEFAULT_CATEGORY, ingredients);
        } else {
            // Si pas d'ingrédients, retourner une liste vide
            ingredientsByCategory.put(DEFAULT_CATEGORY, new ArrayList<>());
        }
        
        return ingredientsByCategory;
    }
}
