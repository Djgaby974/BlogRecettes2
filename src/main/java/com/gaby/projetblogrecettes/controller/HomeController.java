package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.service.CategoryService;
import com.gaby.projetblogrecettes.service.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    private final RecipeService recipeService;
    private final CategoryService categoryService;

    public HomeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Accueil");
        model.addAttribute("recentRecipes", recipeService.getRecentRecipes());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }
}