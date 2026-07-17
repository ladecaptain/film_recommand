package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import com.film.util.TmdbApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MovieService {

    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;
    private final TmdbApiUtil tmdbApiUtil;

    public MovieService(MovieMapper movieMapper, TmdbService tmdbService, TmdbApiUtil tmdbApiUtil) {
        this.movieMapper = movieMapper;
        this.tmdbService = tmdbService;
        this.tmdbApiUtil = tmdbApiUtil;
    }

    public List<Movie> getPopular(int page, int size) {
        List<Movie> movies = tmdbService.getPopularMovies(page);
        if (movies.isEmpty()) {
            movies = fallbackFromDb(page, size, null);
        }
        return movies.stream().map(this::attachPosterUrl).toList();
    }

    public int getPopularPages() {
        int pages = tmdbService.getPopularTotalPages();
        if (pages <= 1) {
            long count = movieMapper.selectCount(null);
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        return pages;
    }

    public List<Movie> search(String keyword, int page, int size) {
        List<Movie> movies = tmdbService.searchMovies(keyword, page);
        if (movies.isEmpty()) {
            Page<Movie> dbPage = movieMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Movie>().like(Movie::getTitle, keyword));
            movies = dbPage.getRecords();
        }
        return movies.stream().map(this::attachPosterUrl).toList();
    }

    public int getSearchPages(String keyword) {
        int pages = tmdbService.getSearchTotalPages(keyword);
        if (pages <= 1) {
            long count = movieMapper.selectCount(
                new LambdaQueryWrapper<Movie>().like(Movie::getTitle, keyword));
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        return pages;
    }

    public List<Movie> discover(String genreIds, String sortBy, String year, int page, int size) {
        List<Movie> movies = tmdbService.discoverMovies(genreIds, sortBy, year, page);
        if (movies.isEmpty()) {
            LambdaQueryWrapper<Movie> wrapper = new LambdaQueryWrapper<>();
            if (genreIds != null && !genreIds.isEmpty()) {
                // 从ID映射到中文名做模糊匹配
                wrapper.and(w -> {
                    for (String gid : genreIds.split(",")) {
                        String cn = TmdbService.GENRE_MAP.get(Integer.parseInt(gid));
                        if (cn != null) w.or().like(Movie::getGenres, cn);
                    }
                });
            }
            wrapper.orderByDesc("vote_average".equals(sortBy) ? Movie::getVoteAverage : Movie::getId);
            Page<Movie> dbPage = movieMapper.selectPage(new Page<>(page, size), wrapper);
            movies = dbPage.getRecords();
        }
        return movies.stream().map(this::attachPosterUrl).toList();
    }

    public int getDiscoverPages(String genreIds, String sortBy, String year) {
        int pages = tmdbService.getDiscoverTotalPages(genreIds, sortBy, year);
        if (pages <= 1) {
            long count = movieMapper.selectCount(null);
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        return pages;
    }

    public Movie getDetail(Long tmdbId) {
        Movie movie = tmdbService.getMovieDetail(tmdbId);
        if (movie == null) {
            movie = movieMapper.selectOne(
                new LambdaQueryWrapper<Movie>().eq(Movie::getTmdbId, tmdbId));
        }
        return attachPosterUrl(movie);
    }

    private List<Movie> fallbackFromDb(int page, int size, String orderBy) {
        Page<Movie> dbPage = movieMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Movie>().orderByDesc(Movie::getVoteCount));
        return dbPage.getRecords();
    }

    private Movie attachPosterUrl(Movie movie) {
        if (movie != null) {
            movie.setPosterUrl(tmdbApiUtil.getImageUrl(movie.getPosterPath(), "w500"));
        }
        return movie;
    }
}
