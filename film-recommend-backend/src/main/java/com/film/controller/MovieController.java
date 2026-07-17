package com.film.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.film.entity.Movie;
import com.film.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/popular")
    public ResponseEntity<Page<Movie>> getPopular(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.getPopular(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Movie>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.search(keyword, page, size));
    }

    @GetMapping("/{tmdbId}")
    public ResponseEntity<Movie> getDetail(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(movieService.getDetail(tmdbId));
    }
}
