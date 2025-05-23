package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.MealPlan;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.MealPlanRepository;
import com.gaby.projetblogrecettes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final UserService userService;

    @Transactional
    public MealPlan addMealToPlan(User user, Long recipeId, LocalDate date, MealPlan.MealType mealType, String notes) {
        log.info("Ajout d'un repas au planning - Utilisateur: {}, Recette: {}, Date: {}, Type: {}", 
                user.getUsername(), recipeId, date, mealType);
                
        // Vérifier si la recette existe
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recette non trouvée avec l'ID: " + recipeId));

        // Vérifier s'il existe déjà un repas planifié pour cette date et ce type de repas
        boolean exists = mealPlanRepository.existsByUserAndMealDateAndMealType(user, date, mealType);
        if (exists) {
            String message = String.format("Un repas est déjà planifié pour le %s (%s) au %s", 
                    mealType, date, user.getUsername());
            log.warn(message);
            throw new IllegalStateException(message);
        }

        // Créer et sauvegarder le nouveau repas planifié
        MealPlan mealPlan = MealPlan.builder()
                .user(user)
                .recipe(recipe)
                .mealDate(date)
                .mealType(mealType)
                .notes(notes)
                .build();

        MealPlan savedMealPlan = mealPlanRepository.save(mealPlan);
        log.info("Repas ajouté avec succès au planning - ID: {}", savedMealPlan.getId());
        
        return savedMealPlan;
    }

    @Transactional(readOnly = true)
    public List<MealPlan> getWeeklyMealPlan(User user, LocalDate startDate) {
        log.debug("Récupération du planning hebdomadaire pour l'utilisateur: {}, Semaine du: {}", 
                user.getUsername(), startDate);
                
        LocalDate endDate = startDate.plusDays(6);
        List<MealPlan> mealPlans = mealPlanRepository
                .findByUserAndMealDateBetweenOrderByMealDateAscMealTypeAsc(user, startDate, endDate);
                
        log.debug("{} repas trouvés pour la période du {} au {}", 
                mealPlans.size(), startDate, endDate);
                
        return mealPlans;
    }

    @Transactional
    public void removeMealFromPlan(String username, Long mealPlanId) {
        log.info("Suppression du plan de repas - Utilisateur: {}, Plan ID: {}", username, mealPlanId);
        
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));
        
        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Plan de repas non trouvé avec l'ID: " + mealPlanId));
        
        if (!mealPlan.getUser().getId().equals(user.getId())) {
            String message = String.format("L'utilisateur %s n'est pas autorisé à supprimer le plan de repas ID: %s", 
                    username, mealPlanId);
            log.warn(message);
            throw new SecurityException(message);
        }
        
        mealPlanRepository.delete(mealPlan);
        log.info("Plan de repas supprimé avec succès - ID: {}", mealPlanId);
    }

    @Transactional(readOnly = true)
    public List<MealPlan> getDailyMealPlan(User user, LocalDate date) {
        log.debug("Récupération du planning quotidien - Utilisateur: {}, Date: {}", 
                user.getUsername(), date);
                
        List<MealPlan> mealPlans = mealPlanRepository.findByUserAndMealDate(user, date);
        log.debug("{} repas trouvés pour le {}", mealPlans.size(), date);
        
        return mealPlans;
    }
}
