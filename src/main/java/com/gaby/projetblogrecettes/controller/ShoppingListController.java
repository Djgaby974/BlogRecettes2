package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.MealPlan;
import com.gaby.projetblogrecettes.service.MealPlanService;
import com.gaby.projetblogrecettes.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final MealPlanService mealPlanService;

    @GetMapping
    public String showRecipesForShoppingList(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long recipeId,
            Authentication authentication,
            Model model) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        
        if (endDate == null) {
            endDate = startDate.plusDays(6); // Par défaut, une semaine
        }
        
        String username = authentication.getName();
        
        // Récupérer les repas planifiés pour la période
        List<MealPlan> mealPlans = mealPlanService.getMealPlansByUserAndDateRange(username, startDate, endDate);
        
        // Si un ID de recette est spécifié, afficher les ingrédients de cette recette
        if (recipeId != null) {
            Map<String, List<String>> ingredients = shoppingListService.getIngredientsByRecipe(mealPlans, recipeId);
            model.addAttribute("ingredientsByCategory", ingredients);
            model.addAttribute("selectedRecipeId", recipeId);
        } else {
            // Sinon, afficher la liste des recettes
            model.addAttribute("recipes", mealPlanService.getUniqueRecipesFromMealPlans(mealPlans));
        }
        
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "shopping-list/list";
    }
}
