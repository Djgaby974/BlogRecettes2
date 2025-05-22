package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.service.CommentService;
import com.gaby.projetblogrecettes.service.RatingService;
import com.gaby.projetblogrecettes.service.RecipeService;
import com.gaby.projetblogrecettes.service.CategoryService;
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
import com.gaby.projetblogrecettes.model.Comment;
import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.model.Recipe;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/recipes")
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;
    private final RatingService ratingService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @Autowired
    public RecipeController(RecipeService recipeService,
                          RatingService ratingService,
                          CommentService commentService,
                          CategoryService categoryService) {
        this.recipeService = recipeService;
        this.ratingService = ratingService;
        this.commentService = commentService;
        this.categoryService = categoryService;
    }

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
    public String showRecipe(@PathVariable Long id, Model model, Principal principal) {
        Recipe recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recette non trouvée"));
        model.addAttribute("recipe", recipe);
        
        boolean isAuthenticated = principal != null;
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        if (isAuthenticated) {
            boolean isAuthor = recipe.getAuthor().getUsername().equals(principal.getName());
            model.addAttribute("isAuthor", isAuthor);
        }
        
        double averageRating = ratingService.getAverageRating(id);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingCount", recipe.getComments().size());
        
        List<Comment> comments = recipe.getComments();
        model.addAttribute("comments", comments);
        
        return "recipes/show";
    }

    @GetMapping
    public String searchRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "true") boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Recipe> recipes = recipeService.searchRecipes(search, categoryId,
                                                         difficulty, published, pageable);
        model.addAttribute("recipes", recipes);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("difficulties", Difficulty.values());
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
        Recipe recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recette non trouvée"));
            
        // Vérifier si l'utilisateur est l'auteur
        if (!recipe.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à modifier cette recette");
        }
        
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails);
        return "redirect:/recipes/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteRecipe(@PathVariable Long id,
                             Principal principal) {
        recipeService.deleteRecipe(id, principal.getName());
        return "redirect:/recipes";
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