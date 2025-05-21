package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.model.RegisterForm;
import org.springframework.validation.BindingResult;
import com.gaby.projetblogrecettes.service.RecipeService;
import com.gaby.projetblogrecettes.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public UserController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserProfile(Principal principal) {
        User user = userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Utilisateur non trouvé"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody User userDetails, 
                                            Principal principal) {
        User updatedUser = userService.updateUser(userDetails, principal.getName());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Ajouter le registerForm au modèle
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }


    @PostMapping("/register")
    public String registerUser(@Valid RegisterForm registerForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            User user = new User();
            user.setUsername(registerForm.getUsername());
            user.setEmail(registerForm.getEmail());
            user.setPassword(registerForm.getPassword());

            userService.registerUser(user);

            // Redirection vers login avec un message de succès
            return "redirect:/api/users/login?success=true";

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email")) {
                result.rejectValue("email", "error.email", "Cet email est déjà utilisé");
            }
            if (e.getMessage().contains("utilisateur")) {
                result.rejectValue("username", "error.username", "Ce nom d'utilisateur est déjà utilisé");
            }
            return "auth/register";
        }
    }



    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";  // Ceci fait référence à login.html
    }

}