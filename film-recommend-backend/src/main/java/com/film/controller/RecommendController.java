package com.film.controller;

import com.film.entity.Movie;
import com.film.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRecommendations(Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(recommendService.recommendForUser(userId));
    }

    @GetMapping("/random")
    public ResponseEntity<Movie> getRandomPick(
            Authentication authentication,
            @RequestParam(required = false) String exclude) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Set<Long> excludeIds = Collections.emptySet();
        if (exclude != null && !exclude.isEmpty()) {
            excludeIds = Arrays.stream(exclude.split(","))
                .map(Long::parseLong).collect(Collectors.toSet());
        }
        Movie pick = recommendService.getRandomPick(userId, excludeIds);
        return pick != null ? ResponseEntity.ok(pick) : ResponseEntity.noContent().build();
    }
}
