package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import com.film.util.TmdbApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MovieService {

    private final MovieMapper movieMapper;
    private final CacheService cacheService;
    private final TmdbApiUtil tmdbApiUtil;

    public MovieService(MovieMapper movieMapper, CacheService cacheService, TmdbApiUtil tmdbApiUtil) {
        this.movieMapper = movieMapper;
        this.cacheService = cacheService;
        this.tmdbApiUtil = tmdbApiUtil;
    }

    public List<Movie> getPopular(int page, int size) {
        return cacheService.getPopularMovies(page).stream()
            .map(this::attachPosterUrl).toList();
    }

    public int getPopularPages() {
        return cacheService.getPopularTotalPages();
    }

    public List<Movie> search(String keyword, int page, int size) {
        return cacheService.getSearchMovies(keyword, page).stream()
            .map(this::attachPosterUrl).toList();
    }

    public int getSearchPages(String keyword) {
        return cacheService.getSearchTotalPages(keyword);
    }

    public List<Movie> discover(String genreIds, String sortBy, String year, int page, int size) {
        return cacheService.getDiscoverMovies(genreIds, sortBy, year, page).stream()
            .map(this::attachPosterUrl).toList();
    }

    public int getDiscoverPages(String genreIds, String sortBy, String year) {
        return cacheService.getDiscoverTotalPages(genreIds, sortBy, year);
    }

    public Movie getDetail(Long tmdbId) {
        Movie movie = cacheService.getMovieDetail(tmdbId);
        return attachPosterUrl(movie);
    }

    private Movie attachPosterUrl(Movie movie) {
        if (movie != null) {
            movie.setPosterUrl(tmdbApiUtil.getImageUrl(movie.getPosterPath(), "w500"));
        }
        return movie;
    }
}
