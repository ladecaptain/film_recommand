package com.film.service;

import com.film.dto.MovieDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TmdbService {

    // Will be fully implemented with RestTemplate calls to TMDb API
    public List<MovieDTO> getPopularMovies(int page) {
        // TODO: Call TMDb /movie/popular
        return new ArrayList<>();
    }

    public List<MovieDTO> searchMovies(String keyword, int page) {
        // TODO: Call TMDb /search/movie
        return new ArrayList<>();
    }

    public MovieDTO getMovieDetail(Long tmdbId) {
        // TODO: Call TMDb /movie/{id}
        return null;
    }
}
