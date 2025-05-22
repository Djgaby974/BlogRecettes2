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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.security.Principal;

@Controller
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    
    @PostConstruct
    public void init() {
        log.info("UserController initialisé avec le chemin de base: /users");
    }
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public UserController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }



    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String getUserProfile(Model model, Principal principal) {
        log.info("=== DÉBUT getUserProfile ===");
        log.info("Utilisateur connecté: " + principal.getName());
        try {
            User user = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Utilisateur non trouvé"));
            log.info("Utilisateur trouvé: " + user.getUsername());
            
            model.addAttribute("user", user);
            model.addAttribute("profileForm", user);
            
            log.info("Attributs du modèle: " + model.asMap().keySet());
            log.info("=== FIN getUserProfile ===");
            return "user/profile";
        } catch (Exception e) {
            log.error("Erreur dans getUserProfile", e);
            throw e;
        }
    }

    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@Valid @ModelAttribute("profileForm") User userDetails,
                              BindingResult result,
                              @RequestParam(required = false) MultipartFile avatarFile,
                              Principal principal,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", userDetails);
            return "user/profile";
        }

        try {
            User updatedUser = userService.updateUser(userDetails, principal.getName());
            redirectAttributes.addFlashAttribute("message", "Profil mis à jour avec succès");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la mise à jour du profil");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/api/users/profile";
    }

    @PostMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    public String updatePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmNewPassword,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("message", "Les mots de passe ne correspondent pas");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/api/users/profile";
        }

        try {
            userService.updatePassword(principal.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "Mot de passe mis à jour avec succès");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la mise à jour du mot de passe");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/api/users/profile";
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