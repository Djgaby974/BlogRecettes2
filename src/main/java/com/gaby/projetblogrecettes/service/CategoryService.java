package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.Category;
import com.gaby.projetblogrecettes.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id: " + id));

        // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre catégorie
        if (!existingCategory.getName().equals(categoryDetails.getName()) &&
            categoryRepository.existsByName(categoryDetails.getName())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }

        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdWithRecipes(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id: " + id));

        if (!category.getRecipes().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une catégorie contenant des recettes");
        }

        categoryRepository.delete(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id: " + id));
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec le nom: " + name));
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
            .orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setName(categoryName);
                return categoryRepository.save(newCategory);
            });
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCategoryStatistics() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
            .collect(Collectors.toMap(
                Category::getName,
                category -> (long) category.getRecipes().size()
            ));
    }

    @Transactional(readOnly = true)
    public List<Category> getPopularCategories(int limit) {
        return categoryRepository.findAll().stream()
            .sorted((c1, c2) -> Integer.compare(
                c2.getRecipes().size(),
                c1.getRecipes().size()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public boolean validateCategory(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }
}