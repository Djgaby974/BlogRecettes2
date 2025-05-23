package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.model.MealPlan;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.service.MealPlanService;
import com.gaby.projetblogrecettes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/meal-plans")
@RequiredArgsConstructor
@Slf4j
public class MealPlanController {
    private final MealPlanService mealPlanService;
    private final UserService userService;

    @GetMapping
    public String getWeeklyMealPlan(
            @RequestParam(required = false) String week,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        try {
            log.info("Récupération du planning hebdomadaire pour l'utilisateur: {}", userDetails.getUsername());
            
            // Déterminer la date de début de la semaine
            LocalDate startDate = (week != null) ? LocalDate.parse(week) : LocalDate.now();
            LocalDate monday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = monday.plusDays(6);
            
            // Récupérer l'utilisateur connecté
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Utilisateur non trouvé: " + userDetails.getUsername()));
            
            // Récupérer les plannings de repas pour la semaine
            List<MealPlan> mealPlans = mealPlanService.getWeeklyMealPlan(user, monday);
            
            // Ajouter les données au modèle
            model.addAttribute("mealPlans", mealPlans);
            model.addAttribute("currentWeekStart", monday);
            model.addAttribute("weekStart", monday);
            model.addAttribute("weekEnd", sunday);
            model.addAttribute("nextWeek", monday.plusWeeks(1));
            model.addAttribute("previousWeek", monday.minusWeeks(1));
            
            // Ajouter la liste des jours de la semaine
            model.addAttribute("weekDays", DayOfWeek.values());
            
            log.debug("Affichage du planning hebdomadaire du {} au {}", monday, sunday);
            return "meal-plans/weekly";
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du planning hebdomadaire", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors du chargement du planning hebdomadaire", e);
        }
    }

    @GetMapping("/day")
    public String getDailyMealPlan(
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        try {
            log.info("Récupération du planning quotidien pour l'utilisateur: {}", userDetails.getUsername());
            
            // Déterminer la date
            LocalDate mealDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
            
            // Récupérer l'utilisateur connecté
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Utilisateur non trouvé: " + userDetails.getUsername()));
            
            // Récupérer les plannings de repas pour la journée
            List<MealPlan> mealPlans = mealPlanService.getDailyMealPlan(user, mealDate);
            
            // Formater la date en français
            String formattedDate = mealDate.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
            
            // Mettre en majuscule la première lettre du jour
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            
            // Ajouter les données au modèle
            model.addAttribute("mealPlans", mealPlans);
            model.addAttribute("selectedDate", mealDate);
            model.addAttribute("formattedDate", formattedDate);
            model.addAttribute("nextDay", mealDate.plusDays(1));
            model.addAttribute("previousDay", mealDate.minusDays(1));
            
            log.debug("Affichage du planning quotidien pour le {}", mealDate);
            return "meal-plans/daily";
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du planning quotidien", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors du chargement du planning quotidien", e);
        }
    }

    @PostMapping
    public String addMealToPlan(
            @RequestParam Long recipeId,
            @RequestParam LocalDate date,
            @RequestParam MealPlan.MealType mealType,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            log.info("Ajout d'un repas au planning - Utilisateur: {}, Recette: {}, Date: {}, Type: {}", 
                    userDetails.getUsername(), recipeId, date, mealType);
            
            // Récupérer l'utilisateur connecté
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Utilisateur non trouvé: " + userDetails.getUsername()));
            
            // Ajouter le repas au planning
            mealPlanService.addMealToPlan(user, recipeId, date, mealType, notes);
            
            log.info("Repas ajouté avec succès au planning");
            
            // Rediriger vers la vue quotidienne avec la date du repas ajouté
            return "redirect:/meal-plans/day?date=" + date.toString();
            
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout d'un repas au planning", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors de l'ajout du repas au planning", e);
        }
    }

    @PostMapping("/{id}/delete")
    public String removeMealFromPlan(
            @PathVariable Long id,
            @RequestParam(required = false) String redirectTo,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            log.info("Suppression du repas ID: {} par l'utilisateur: {}", id, userDetails.getUsername());
            
            // Supprimer le repas du planning
            mealPlanService.removeMealFromPlan(userDetails.getUsername(), id);
            
            log.info("Repas supprimé avec succès - ID: {}", id);
            
            // Déterminer la redirection
            if ("week".equals(redirectTo)) {
                return "redirect:/meal-plans";
            } else {
                return "redirect:/meal-plans/day";
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du repas ID: " + id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur est survenue lors de la suppression du repas", e);
        }
    }
}
