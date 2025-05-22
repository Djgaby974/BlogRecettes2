package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.model.Difficulty;
import com.gaby.projetblogrecettes.repository.RatingRepository;
import com.gaby.projetblogrecettes.service.CommentService;
import com.gaby.projetblogrecettes.service.RatingService;
import com.gaby.projetblogrecettes.service.UserService;
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
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @Autowired
    public RecipeController(RecipeService recipeService,
                          RatingService ratingService,
                          RatingRepository ratingRepository,
                          UserService userService,
                          CommentService commentService,
                          CategoryService categoryService) {
        this.recipeService = recipeService;
        this.ratingService = ratingService;
        this.ratingRepository = ratingRepository;
        this.userService = userService;
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
            
            // Vérifier si l'utilisateur a déjà noté cette recette
            User currentUser = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
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