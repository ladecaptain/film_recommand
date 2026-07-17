package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.film.dto.MovieDTO;
import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import com.film.util.TmdbApiUtil;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final MovieMapper movieMapper;
    private final TmdbApiUtil tmdbApiUtil;

    public MovieService(MovieMapper movieMapper, TmdbApiUtil tmdbApiUtil) {
        this.movieMapper = movieMapper;
        this.tmdbApiUtil = tmdbApiUtil;
    }

    public Page<Movie> getPopular(int page, int size) {
        return movieMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Movie>().orderByDesc(Movie::getVoteAverage)
        );
    }

    public Movie getDetail(Long tmdbId) {
        return movieMapper.selectOne(new LambdaQueryWrapper<Movie>().eq(Movie::getTmdbId, tmdbId));
    }

    public Page<Movie> search(String keyword, int page, int size) {
        return movieMapper.selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<Movie>()
                .like(Movie::getTitle, keyword)
                .orderByDesc(Movie::getVoteAverage)
        );
    }
}
