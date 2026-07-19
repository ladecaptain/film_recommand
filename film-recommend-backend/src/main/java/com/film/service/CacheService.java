package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheService {

    private static final Duration POPULAR_TTL = Duration.ofMinutes(30);
    private static final Duration DISCOVER_TTL = Duration.ofMinutes(20);
    private static final Duration SEARCH_TTL = Duration.ofMinutes(10);
    private static final Duration DETAIL_TTL = Duration.ofMinutes(60);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;

    public CacheService(RedisTemplate<String, Object> redisTemplate,
                        MovieMapper movieMapper, TmdbService tmdbService) {
        this.redisTemplate = redisTemplate;
        this.movieMapper = movieMapper;
        this.tmdbService = tmdbService;
    }

    // ---- Popular ----

    public List<Movie> getPopularMovies(int page) {
        String key = "movie:popular:page:" + page;
        List<Movie> cached = getList(key);
        if (cached != null) {
            log.debug("Redis hit: {}", key);
            return cached;
        }

        List<Movie> movies = tmdbService.getPopularMovies(page);
        if (movies.isEmpty()) {
            log.info("TMDb 不可用，回退到本地 MySQL 热门");
            movies = fallbackPopularFromDb(page);
        }

        if (!movies.isEmpty()) {
            setList(key, movies, POPULAR_TTL);
        }
        return movies;
    }

    public int getPopularTotalPages() {
        String key = "movie:popular:totalPages";
        Integer cached = getInt(key);
        if (cached != null) return cached;

        int pages = tmdbService.getPopularTotalPages();
        if (pages <= 1) {
            long count = movieMapper.selectCount(null);
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        setInt(key, pages, POPULAR_TTL);
        return pages;
    }

    // ---- Discover ----

    public List<Movie> getDiscoverMovies(String genreIds, String sortBy, String year, int page) {
        String hash = buildDiscoverHash(genreIds, sortBy, year);
        String key = "movie:discover:" + hash + ":page:" + page;
        List<Movie> cached = getList(key);
        if (cached != null) {
            log.debug("Redis hit: {}", key);
            return cached;
        }

        List<Movie> movies = tmdbService.discoverMovies(genreIds, sortBy, year, page);
        if (movies.isEmpty()) {
            log.info("TMDb 不可用，回退到本地 MySQL 筛选");
            movies = fallbackDiscoverFromDb(genreIds, sortBy, page);
        }

        if (!movies.isEmpty()) {
            setList(key, movies, DISCOVER_TTL);
        }
        return movies;
    }

    public int getDiscoverTotalPages(String genreIds, String sortBy, String year) {
        String hash = buildDiscoverHash(genreIds, sortBy, year);
        String key = "movie:discover:" + hash + ":totalPages";
        Integer cached = getInt(key);
        if (cached != null) return cached;

        int pages = tmdbService.getDiscoverTotalPages(genreIds, sortBy, year);
        if (pages <= 1) {
            long count = movieMapper.selectCount(null);
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        setInt(key, pages, DISCOVER_TTL);
        return pages;
    }

    // ---- Search ----

    public List<Movie> getSearchMovies(String keyword, int page) {
        String key = "movie:search:" + keyword + ":page:" + page;
        List<Movie> cached = getList(key);
        if (cached != null) {
            log.debug("Redis hit: {}", key);
            return cached;
        }

        List<Movie> movies = tmdbService.searchMovies(keyword, page);
        if (movies.isEmpty()) {
            log.info("TMDb 不可用，回退到本地 MySQL 搜索");
            movies = fallbackSearchFromDb(keyword, page);
        }

        if (!movies.isEmpty()) {
            setList(key, movies, SEARCH_TTL);
        }
        return movies;
    }

    public int getSearchTotalPages(String keyword) {
        String key = "movie:search:" + keyword + ":totalPages";
        Integer cached = getInt(key);
        if (cached != null) return cached;

        int pages = tmdbService.getSearchTotalPages(keyword);
        if (pages <= 1) {
            long count = movieMapper.selectCount(
                new LambdaQueryWrapper<Movie>().like(Movie::getTitle, keyword));
            pages = Math.max(1, (int) Math.ceil((double) count / 20));
        }
        setInt(key, pages, SEARCH_TTL);
        return pages;
    }

    // ---- Detail ----

    public Movie getMovieDetail(Long tmdbId) {
        String key = "movie:detail:" + tmdbId;
        Movie cached = getMovie(key);
        if (cached != null) {
            log.debug("Redis hit: {}", key);
            return cached;
        }

        Movie movie = tmdbService.getMovieDetail(tmdbId);
        if (movie == null) {
            log.info("TMDb 不可用，回退到本地 MySQL 详情");
            movie = movieMapper.selectOne(
                new LambdaQueryWrapper<Movie>().eq(Movie::getTmdbId, tmdbId));
        }

        if (movie != null) {
            setMovie(key, movie, DETAIL_TTL);
        }
        return movie;
    }

    // ---- Scheduled refresh ----

    /**
     * 定时任务调用：从 TMDb 刷新热门数据到 Redis + MySQL。
     */
    public void refreshPopularCache() {
        log.info("开始刷新热门电影缓存...");
        for (int page = 1; page <= 3; page++) {
            try {
                List<Movie> movies = tmdbService.getPopularMovies(page);
                if (!movies.isEmpty()) {
                    setList("movie:popular:page:" + page, movies, POPULAR_TTL);
                    log.debug("已刷新热门 page {}", page);
                }
            } catch (Exception e) {
                log.error("刷新热门 page {} 失败", page, e);
            }
        }
        // 刷新总页数
        int pages = tmdbService.getPopularTotalPages();
        if (pages > 1) {
            setInt("movie:popular:totalPages", pages, POPULAR_TTL);
        }
        log.info("热门电影缓存刷新完成");
    }

    // ---- Redis helpers ----

    @SuppressWarnings("unchecked")
    private List<Movie> getList(String key) {
        try {
            Object val = redisTemplate.opsForValue().get(key);
            return val instanceof List ? (List<Movie>) val : null;
        } catch (Exception e) {
            log.warn("Redis 读取失败: {}", key, e);
            return null;
        }
    }

    private void setList(String key, List<Movie> movies, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, movies, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 写入失败: {}", key, e);
        }
    }

    private Movie getMovie(String key) {
        try {
            Object val = redisTemplate.opsForValue().get(key);
            return val instanceof Movie ? (Movie) val : null;
        } catch (Exception e) {
            log.warn("Redis 读取失败: {}", key, e);
            return null;
        }
    }

    private void setMovie(String key, Movie movie, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, movie, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 写入失败: {}", key, e);
        }
    }

    private Integer getInt(String key) {
        try {
            Object val = redisTemplate.opsForValue().get(key);
            return val instanceof Number ? ((Number) val).intValue() : null;
        } catch (Exception e) {
            log.warn("Redis 读取失败: {}", key, e);
            return null;
        }
    }

    private void setInt(String key, int value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 写入失败: {}", key, e);
        }
    }

    // ---- MySQL fallbacks ----

    private List<Movie> fallbackPopularFromDb(int page) {
        Page<Movie> dbPage = movieMapper.selectPage(
            new Page<>(page, 20),
            new LambdaQueryWrapper<Movie>().orderByDesc(Movie::getVoteCount));
        return dbPage.getRecords();
    }

    private List<Movie> fallbackDiscoverFromDb(String genreIds, String sortBy, int page) {
        LambdaQueryWrapper<Movie> wrapper = new LambdaQueryWrapper<>();
        if (genreIds != null && !genreIds.isEmpty()) {
            wrapper.and(w -> {
                for (String gid : genreIds.split(",")) {
                    String cn = TmdbService.GENRE_MAP.get(Integer.parseInt(gid.trim()));
                    if (cn != null) w.or().like(Movie::getGenres, cn);
                }
            });
        }
        wrapper.orderByDesc("vote_average".equals(sortBy) ? Movie::getVoteAverage : Movie::getId);
        return movieMapper.selectPage(new Page<>(page, 20), wrapper).getRecords();
    }

    private List<Movie> fallbackSearchFromDb(String keyword, int page) {
        return movieMapper.selectPage(
            new Page<>(page, 20),
            new LambdaQueryWrapper<Movie>().like(Movie::getTitle, keyword)).getRecords();
    }

    private String buildDiscoverHash(String genreIds, String sortBy, String year) {
        StringBuilder sb = new StringBuilder();
        sb.append(genreIds != null ? genreIds : "all");
        sb.append('_').append(sortBy != null ? sortBy : "popularity.desc");
        sb.append('_').append(year != null ? year : "all");
        return sb.toString();
    }
}
