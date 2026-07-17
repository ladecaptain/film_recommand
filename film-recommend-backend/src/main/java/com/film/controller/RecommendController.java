package com.film.controller;

import com.film.entity.Movie;
import com.film.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getRecommendations(Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(recommendService.recommendForUser(userId));
    }

    @GetMapping("/random")
    public ResponseEntity<Movie> getRandomPick(Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(recommendService.getRandomPick(userId));
    }
}
