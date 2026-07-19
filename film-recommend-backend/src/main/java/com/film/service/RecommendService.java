package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.film.entity.Movie;
import com.film.entity.WatchRecord;
import com.film.mapper.MovieMapper;
import com.film.mapper.RecordMapper;
import com.film.util.TmdbApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendService {

    private final RecordMapper recordMapper;
    private final MovieMapper movieMapper;
    private final CacheService cacheService;
    private final TmdbApiUtil tmdbApiUtil;

    public RecommendService(RecordMapper recordMapper, MovieMapper movieMapper,
                            CacheService cacheService, TmdbApiUtil tmdbApiUtil) {
        this.recordMapper = recordMapper;
        this.movieMapper = movieMapper;
        this.cacheService = cacheService;
        this.tmdbApiUtil = tmdbApiUtil;
    }

    public Map<String, Object> recommendForUser(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (userId == null) {
            result.put("reason", "为你精选热门佳作");
            result.put("topGenres", Collections.emptyList());
            result.put("movies", enrichAndLimit(cacheService.getPopularMovies(1), 10));
            return result;
        }

        // 1. 获取用户所有记录，分别统计"看过"和"想看"的类型偏好
        List<WatchRecord> allUserRecords = recordMapper.selectList(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId));

        List<WatchRecord> watched = allUserRecords.stream()
            .filter(r -> r.getStatus() == 2).toList();
        List<WatchRecord> wishlist = allUserRecords.stream()
            .filter(r -> r.getStatus() == 1).toList();

        if (watched.isEmpty() && wishlist.isEmpty()) {
            result.put("reason", "为你精选热门佳作");
            result.put("topGenres", Collections.emptyList());
            result.put("movies", enrichAndLimit(cacheService.getPopularMovies(1), 10));
            return result;
        }

        // 统计类型频次："看过"权重 2，"想看"权重 1
        Map<String, Integer> genreCount = new LinkedHashMap<>();
        for (WatchRecord r : watched) {
            Movie m = movieMapper.selectById(r.getMovieId());
            if (m == null || m.getGenres() == null) continue;
            for (String g : m.getGenres().split(",")) {
                String trimmed = g.trim();
                if (!trimmed.isEmpty()) genreCount.merge(trimmed, 2, Integer::sum);
            }
        }
        for (WatchRecord r : wishlist) {
            Movie m = movieMapper.selectById(r.getMovieId());
            if (m == null || m.getGenres() == null) continue;
            for (String g : m.getGenres().split(",")) {
                String trimmed = g.trim();
                if (!trimmed.isEmpty()) genreCount.merge(trimmed, 1, Integer::sum);
            }
        }

        List<String> topGenres = genreCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (topGenres.isEmpty()) {
            result.put("reason", "为你精选热门佳作");
            result.put("topGenres", Collections.emptyList());
            result.put("movies", enrichAndLimit(cacheService.getPopularMovies(1), 10));
            return result;
        }

        // 2. 排除已看/想看的电影
        Set<Long> excludeLocalIds = allUserRecords.stream()
            .map(WatchRecord::getMovieId)
            .collect(Collectors.toSet());

        // 3. 从 TMDb 按每个 Top 类型拉取候选，汇总后去重
        Map<Long, Movie> candidates = new LinkedHashMap<>();
        for (String genre : topGenres) {
            Integer genreId = TmdbService.GENRE_ID_MAP.get(genre);
            if (genreId == null) continue;
            List<Movie> list = cacheService.getDiscoverMovies(
                String.valueOf(genreId), "vote_average.desc", null, 1);
            for (Movie m : list) {
                if (m.getVoteAverage() != null
                    && m.getVoteAverage().compareTo(BigDecimal.valueOf(7.0)) >= 0
                    && !excludeLocalIds.contains(m.getId())) {
                    candidates.putIfAbsent(m.getTmdbId(), m);
                }
            }
        }

        // 4. 如果 TMDb 不可用，回退到本地库
        if (candidates.isEmpty()) {
            LambdaQueryWrapper<Movie> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Movie::getVoteAverage, BigDecimal.valueOf(7.0));
            if (!excludeLocalIds.isEmpty()) {
                wrapper.notIn(Movie::getId, excludeLocalIds);
            }
            List<Movie> local = movieMapper.selectList(wrapper);
            candidates = local.stream()
                .collect(Collectors.toMap(Movie::getTmdbId, m -> m, (a, b) -> a, LinkedHashMap::new));
        }

        // 5. 计算匹配度
        List<Movie> scored = new ArrayList<>();
        for (Movie m : candidates.values()) {
            int matchCount = 0;
            String bestMatchGenre = topGenres.get(0);
            if (m.getGenres() != null) {
                for (String g : topGenres) {
                    if (m.getGenres().contains(g)) {
                        matchCount++;
                        if (matchCount == 1) bestMatchGenre = g;
                    }
                }
            }
            if (matchCount == 0) continue;

            int score = (int) Math.round((double) matchCount / topGenres.size() * 100);
            m.setMatchScore(score);
            m.setMatchGenre(bestMatchGenre);
            scored.add(m);
        }

        // 6. 排序：匹配度 desc → 评分 desc
        scored.sort(Comparator
            .comparingInt((Movie m) -> m.getMatchScore() != null ? m.getMatchScore() : 0).reversed()
            .thenComparing((Movie m) -> m.getVoteAverage() != null ? m.getVoteAverage() : BigDecimal.ZERO,
                Comparator.reverseOrder()));

        List<Movie> top10 = scored.stream().limit(10).collect(Collectors.toList());

        // 附加 posterUrl
        for (Movie m : top10) {
            m.setPosterUrl(tmdbApiUtil.getImageUrl(m.getPosterPath(), "w500"));
        }

        // 拼接推荐理由
        String reason = "因为你常看" + String.join("、", topGenres) + "，推荐这些高分佳作";
        if (scored.size() > 10) {
            reason += "（共找到 " + scored.size() + " 部匹配）";
        }

        result.put("reason", reason);
        result.put("topGenres", topGenres);
        result.put("movies", top10);
        return result;
    }

    private List<Movie> enrichAndLimit(List<Movie> movies, int limit) {
        return movies.stream().limit(limit).peek(m ->
            m.setPosterUrl(tmdbApiUtil.getImageUrl(m.getPosterPath(), "w500"))
        ).collect(Collectors.toList());
    }

    public Movie getRandomPick(Long userId) {
        return getRandomPick(userId, Collections.emptySet());
    }

    public Movie getRandomPick(Long userId, Set<Long> excludeTmdbIds) {
        if (userId == null) {
            List<Movie> popular = cacheService.getPopularMovies(1);
            popular.removeIf(m -> excludeTmdbIds.contains(m.getTmdbId()));
            if (!popular.isEmpty()) {
                Movie m = popular.get((int) (Math.random() * popular.size()));
                m = enrichWithDetail(m);
                m.setPosterUrl(tmdbApiUtil.getImageUrl(m.getPosterPath(), "w500"));
                return m;
            }
            return null;
        }

        // 获取用户所有记录
        List<WatchRecord> allUserRecords = recordMapper.selectList(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId));
        Set<Long> excludeLocalIds = allUserRecords.stream()
            .map(WatchRecord::getMovieId).collect(Collectors.toSet());

        // 统计类型偏好（看过+想看）
        Map<String, Integer> genreCount = new LinkedHashMap<>();
        for (WatchRecord r : allUserRecords) {
            Movie m = movieMapper.selectById(r.getMovieId());
            if (m == null || m.getGenres() == null) continue;
            int weight = r.getStatus() == 2 ? 2 : 1;
            for (String g : m.getGenres().split(",")) {
                String t = g.trim();
                if (!t.isEmpty()) genreCount.merge(t, weight, Integer::sum);
            }
        }

        List<String> topGenres = genreCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (topGenres.isEmpty()) {
            topGenres = List.of("动作", "科幻", "剧情");
        }

        // 构建大候选池：每个 Top 类型拉 3 页
        Map<Long, Movie> pool = new LinkedHashMap<>();
        for (String genre : topGenres) {
            Integer genreId = TmdbService.GENRE_ID_MAP.get(genre);
            if (genreId == null) continue;
            for (int page = 1; page <= 3; page++) {
                List<Movie> list = cacheService.getDiscoverMovies(
                    String.valueOf(genreId), "popularity.desc", null, page);
                for (Movie m : list) {
                    if (m.getVoteAverage() != null
                        && m.getVoteAverage().compareTo(BigDecimal.valueOf(6.5)) >= 0
                        && !excludeLocalIds.contains(m.getId())
                        && !excludeTmdbIds.contains(m.getTmdbId())) {
                        pool.putIfAbsent(m.getTmdbId(), m);
                    }
                }
            }
        }

        // 候选不够则补充热门
        if (pool.size() < 10) {
            for (int page = 1; page <= 2; page++) {
                for (Movie m : cacheService.getPopularMovies(page)) {
                    if (!excludeLocalIds.contains(m.getId())
                        && !excludeTmdbIds.contains(m.getTmdbId())) {
                        pool.putIfAbsent(m.getTmdbId(), m);
                    }
                }
            }
        }

        if (pool.isEmpty()) {
            List<Movie> popular = cacheService.getPopularMovies(1);
            if (!popular.isEmpty()) {
                Movie m = popular.get((int) (Math.random() * popular.size()));
                m = enrichWithDetail(m);
                m.setPosterUrl(tmdbApiUtil.getImageUrl(m.getPosterPath(), "w500"));
                return m;
            }
            return null;
        }

        List<Movie> candidates = new ArrayList<>(pool.values());
        Movie pick = candidates.get((int) (Math.random() * candidates.size()));
        pick = enrichWithDetail(pick);
        pick.setPosterUrl(tmdbApiUtil.getImageUrl(pick.getPosterPath(), "w500"));
        return pick;
    }

    private Movie enrichWithDetail(Movie m) {
        Movie detail = cacheService.getMovieDetail(m.getTmdbId());
        if (detail != null) {
            if (detail.getOverview() != null && !detail.getOverview().isEmpty()) {
                m.setOverview(detail.getOverview());
            }
            if (detail.getDirector() != null) m.setDirector(detail.getDirector());
            if (detail.getCast() != null) m.setCast(detail.getCast());
            if (detail.getRuntime() != null) m.setRuntime(detail.getRuntime());
        }
        return m;
    }
}
