package com.gaby.projetblogrecettes.repository;

import com.gaby.projetblogrecettes.model.MealPlan;
import com.gaby.projetblogrecettes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByUserAndMealDateBetweenOrderByMealDateAscMealTypeAsc(
            User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT mp FROM MealPlan mp WHERE mp.user = :user AND mp.mealDate = :date ORDER BY mp.mealType")
    List<MealPlan> findByUserAndMealDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT CASE WHEN COUNT(mp) > 0 THEN true ELSE false END FROM MealPlan mp " +
           "WHERE mp.user = :user AND mp.mealDate = :date AND mp.mealType = :mealType")
    boolean existsByUserAndMealDateAndMealType(
            @Param("user") User user, 
            @Param("date") LocalDate date, 
            @Param("mealType") MealPlan.MealType mealType);
            
    @Query("SELECT CASE WHEN COUNT(mp) > 0 THEN true ELSE false END FROM MealPlan mp " +
           "WHERE mp.user = :user AND mp.recipe.id = :recipeId AND mp.mealDate = :date AND mp.mealType = :mealType")
    boolean existsByUserAndRecipeAndMealDateAndMealType(
            @Param("user") User user,
            @Param("recipeId") Long recipeId,
            @Param("date") LocalDate date,
            @Param("mealType") MealPlan.MealType mealType);
}
