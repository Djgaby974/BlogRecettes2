package com.gaby.projetblogrecettes.controller;

import com.gaby.projetblogrecettes.service.CommentService;
import com.gaby.projetblogrecettes.service.RatingService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/")
public class RatingCommentController {

    private final RatingService ratingService;
    private final CommentService commentService;

    public RatingCommentController(RatingService ratingService, CommentService commentService) {
        this.ratingService = ratingService;
        this.commentService = commentService;
    }

    @GetMapping("/ratings")
    public String showRatings(Model model, @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("ratingsPage", ratingService.getAllRatings(pageable));
        return "ratings/list";
    }

    @GetMapping("/comments")
    public String showComments(Model model, @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("commentsPage", commentService.getAllComments(pageable));
        return "comments/list";
    }
    
    @PostMapping("/comments/{parentId}/reply")
    public String addReply(@PathVariable Long parentId, 
                          @RequestParam String content,
                          Principal principal) {
        commentService.addReply(parentId, principal.getName(), content);
        return "redirect:/comments";
    }
}
