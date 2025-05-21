package com.gaby.projetblogrecettes.service;

import com.gaby.projetblogrecettes.model.Comment;
import com.gaby.projetblogrecettes.model.Recipe;
import com.gaby.projetblogrecettes.model.User;
import com.gaby.projetblogrecettes.repository.CommentRepository;
import com.gaby.projetblogrecettes.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final UserService userService;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                         RecipeRepository recipeRepository,
                         UserService userService) {
        this.commentRepository = commentRepository;
        this.recipeRepository = recipeRepository;
        this.userService = userService;
    }

    public Comment addComment(Long recipeId, String username, String content) {
        User user = userService.getUserByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RuntimeException("Recette non trouvée"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setRecipe(recipe);
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    public Page<Comment> getRecipeComments(Long recipeId, Pageable pageable) {
        return commentRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId, pageable);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Non autorisé à supprimer ce commentaire");
        }

        commentRepository.delete(comment);
    }
}