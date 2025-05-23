package com.gaby.projetblogrecettes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "meal_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class MealPlan implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @ToString.Exclude
    private Recipe recipe;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum MealType {
        PETIT_DEJEUNER("Petit-déjeuner"),
        DEJEUNER("Déjeuner"),
        DINER("Dîner"),
        DESSERT("Dessert"),
        COLLATION("Collation");
        
        private final String displayName;
        
        MealType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static MealType fromDisplayName(String displayName) {
            for (MealType type : values()) {
                if (type.displayName.equalsIgnoreCase(displayName)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Type de repas non valide: " + displayName);
        }
    }
    
    /**
     * Vérifie si ce repas planifié est pour aujourd'hui
     */
    public boolean isToday() {
        return mealDate != null && mealDate.isEqual(LocalDate.now());
    }
    
    /**
     * Vérifie si ce repas planifié est pour demain
     */
    public boolean isTomorrow() {
        return mealDate != null && mealDate.isEqual(LocalDate.now().plusDays(1));
    }
    
    /**
     * Formate la date du repas selon le format spécifié
     * @param pattern Le modèle de formatage (par exemple "EEEE d MMMM yyyy")
     * @return La date formatée en chaîne
     */
    public String formatMealDate(String pattern) {
        if (mealDate == null) return "";
        return mealDate.format(java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.FRENCH));
    }
}
