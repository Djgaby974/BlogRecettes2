package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.Comment;
import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.RatingRepository;
import com.gaby.projetblogrecettes.service.CategoryService;
import com.gaby.projetblogrecettes.service.CommentService;
import com.gaby.projetblogrecettes.service.RatingService;
import com.gaby.projetblogrecettes.service.RecipeService;
import com.gaby.projetblogrecettes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recipes")
@Slf4j
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final RatingService ratingService;
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String showCreateForm(Model model) {
        model.addAttribute("recipe", new Recipe());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("difficulties", Difficulty.values());
        return "recipes/create";
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String createRecipe(@ModelAttribute Recipe recipe,
                             Principal principal, Model model) {
        Recipe newRecipe = recipeService.createRecipe(recipe, principal.getName());
        return "redirect:/recipes/" + newRecipe.getId();
    }

    @GetMapping("/{id}")
    public String viewRecipe(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean addToMealPlan,
            @RequestParam(required = false) String mealDate,
            @RequestParam(required = false) String mealType,
            Model model, Principal principal) {
        try {
            log.info("Affichage de la recette ID: {}", id);
            
            model.addAttribute("addToMealPlan", Boolean.TRUE.equals(addToMealPlan));
            model.addAttribute("mealDate", mealDate);
            model.addAttribute("mealType", mealType);
            
            Recipe recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recette non trouvée avec l'ID: " + id));
            model.addAttribute("recipe", recipe);
            
            boolean isAuthenticated = principal != null;
            model.addAttribute("isAuthenticated", isAuthenticated);
            
            if (isAuthenticated) {
                boolean isAuthor = recipe.getAuthor().getUsername().equals(principal.getName());
                model.addAttribute("isAuthor", isAuthor);
                
                // Vérifier si l'utilisateur a déjà noté cette recette
                User currentUser = userService.getUserByUsername(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé: " + principal.getName()));
                boolean hasRated = ratingRepository.findByUserAndRecipe(currentUser, recipe).isPresent();
                model.addAttribute("hasRated", hasRated);
            } else {
                model.addAttribute("hasRated", false);
            }
            
            double averageRating = ratingService.getAverageRating(id);
            model.addAttribute("averageRating", averageRating);
            
            // Charger explicitement les commentaires avec leurs utilisateurs
            List<Comment> comments = commentService.getCommentsByRecipeId(id);
            model.addAttribute("comments", comments);
            model.addAttribute("ratingCount", comments.size());
            
            return "recipes/show";
            
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage de la recette ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors du chargement de la recette", e);
        }
    }

    @GetMapping
    public String searchRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean addToMealPlan,
            @RequestParam(required = false) String mealDate,
            @RequestParam(required = false) String mealType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Si published est null, on ne filtre pas par statut de publication
        Boolean publishedFilter = published != null ? published : null;
        Page<Recipe> recipes = recipeService.searchRecipes(search, categoryId,
                                                         difficulty, publishedFilter, pageable);
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("difficulties", Difficulty.values());
        
        // Ajout des attributs pour le planificateur de repas
        model.addAttribute("addToMealPlan", addToMealPlan != null && addToMealPlan);
        model.addAttribute("mealDate", mealDate);
        model.addAttribute("mealType", mealType);
        
        if (addToMealPlan != null && addToMealPlan) {
            model.addAttribute("pageTitle", "Choisir une recette pour le planificateur");
            return "recipes/select-for-meal-plan";
        }
        
        return "recipes/list";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Recipe recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recette non trouvée"));
            
        // Vérifier si l'utilisateur est l'auteur
        if (!recipe.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à modifier cette recette");
        }
        
        model.addAttribute("recipe", recipe);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("difficulties", Difficulty.values());
        return "recipes/edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateRecipe(@PathVariable Long id,
                             @ModelAttribute Recipe recipeDetails,
                             Principal principal) {
        try {
            log.info("Mise à jour de la recette ID: {}", id);
            
            Recipe recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Recette non trouvée avec l'ID: " + id));
                
            // Vérifier si l'utilisateur est l'auteur
            if (!recipe.getAuthor().getUsername().equals(principal.getName())) {
                log.warn("Tentative non autorisée de modification de la recette ID: {} par l'utilisateur: {}", 
                    id, principal.getName());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Vous n'êtes pas autorisé à modifier cette recette");
            }
            
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails);
            log.info("Recette mise à jour avec succès - ID: {}", id);
            return "redirect:/recipes/" + id;
            
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la recette ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors de la mise à jour de la recette", e);
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteRecipe(@PathVariable Long id, Principal principal) {
        try {
            log.info("Suppression de la recette ID: {} par l'utilisateur: {}", id, principal.getName());
            
            recipeService.deleteRecipe(id, principal.getName());
            log.info("Recette supprimée avec succès - ID: {}", id);
            
            return "redirect:/recipes";
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la recette ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors de la suppression de la recette", e);
        }
    }
    
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long id,
                           @RequestParam String content,
                           Principal principal) {
        commentService.addComment(id, principal.getName(), content);
        return "redirect:/recipes/" + id + "#comments";
    }
    
    @PostMapping("/{id}/rate")
    @PreAuthorize("isAuthenticated()")
    public String rateRecipe(@PathVariable Long id,
                           @RequestParam int rating,
                           Principal principal) {
        ratingService.rateRecipe(id, principal.getName(), rating);
        return "redirect:/recipes/" + id + "#rating";
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public String myRecipes(Model model, Principal principal,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Recipe> recipes = recipeService.getRecipesByAuthor(principal.getName(), pageable);
        model.addAttribute("recipes", recipes);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("difficulties", Difficulty.values());
        return "recipes/list";
    }
}