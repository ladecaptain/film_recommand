package com.film.controller;

import com.film.entity.Movie;
import com.film.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopular(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Movie> movies = movieService.getPopular(page, size);
        int totalPages = movieService.getPopularPages();
        return ResponseEntity.ok(Map.of(
            "records", movies,
            "total", 20 * totalPages,
            "current", page,
            "pages", totalPages
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Movie> movies = movieService.search(keyword, page, size);
        int totalPages = movieService.getSearchPages(keyword);
        return ResponseEntity.ok(Map.of(
            "records", movies,
            "total", 20 * totalPages,
            "current", page,
            "pages", totalPages
        ));
    }

    @GetMapping("/discover")
    public ResponseEntity<Map<String, Object>> discover(
            @RequestParam(required = false) String genres,
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "popularity.desc") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Movie> movies = movieService.discover(genres, sortBy, year, page, size);
        int totalPages = movieService.getDiscoverPages(genres, sortBy, year);
        return ResponseEntity.ok(Map.of(
            "records", movies,
            "total", 20 * totalPages,
            "current", page,
            "pages", totalPages
        ));
    }

    @GetMapping("/{tmdbId}")
    public ResponseEntity<Movie> getDetail(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(movieService.getDetail(tmdbId));
    }
}
