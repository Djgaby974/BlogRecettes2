package com.gaby.projetblogrecettes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    @ElementCollection
    private List<String> ingredients = new ArrayList<>();

    @Column(length = 5000)
    private String instructions;

    @Column(name = "prep_time")
    private Integer prepTime; // en minutes

    @Column(name = "cook_time")
    private Integer cookTime; // en minutes

    @Column(name = "serving_size")
    private Integer servingSize;

    private String difficulty; // FACILE, MOYEN, DIFFICILE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private boolean published;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();
}