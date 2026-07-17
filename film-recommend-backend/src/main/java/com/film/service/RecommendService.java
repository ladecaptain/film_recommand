package com.film.service;

import com.film.entity.Movie;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendService {

    private final MovieService movieService;

    public RecommendService(MovieService movieService) {
        this.movieService = movieService;
    }

    public List<Movie> recommendForUser(Long userId) {
        // TODO: 实现推荐算法
        // 1. 查询用户最高频的 Top 3 类型
        // 2. 排除已看过的电影
        // 3. 按评分排序返回 Top 10
        return movieService.getPopular(1, 10).getRecords();
    }

    public Movie getRandomPick(Long userId) {
        List<Movie> candidates = recommendForUser(userId);
        if (candidates.isEmpty()) {
            candidates = movieService.getPopular(1, 10).getRecords();
        }
        return candidates.get((int) (Math.random() * candidates.size()));
    }
}
