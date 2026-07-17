package com.film.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.film.entity.Movie;
import com.film.mapper.MovieMapper;
import com.film.util.TmdbApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TmdbService {

    public static final Map<Integer, String> GENRE_MAP = new LinkedHashMap<>();
    static {
        GENRE_MAP.put(28, "动作"); GENRE_MAP.put(12, "冒险"); GENRE_MAP.put(16, "动画");
        GENRE_MAP.put(35, "喜剧"); GENRE_MAP.put(80, "犯罪"); GENRE_MAP.put(99, "纪录");
        GENRE_MAP.put(18, "剧情"); GENRE_MAP.put(10751, "家庭"); GENRE_MAP.put(14, "奇幻");
        GENRE_MAP.put(36, "历史"); GENRE_MAP.put(27, "恐怖"); GENRE_MAP.put(10402, "音乐");
        GENRE_MAP.put(9648, "悬疑"); GENRE_MAP.put(10749, "爱情"); GENRE_MAP.put(878, "科幻");
        GENRE_MAP.put(10770, "电视电影"); GENRE_MAP.put(53, "惊悚"); GENRE_MAP.put(10752, "战争");
        GENRE_MAP.put(37, "西部");
    }

    private final TmdbApiUtil tmdb;
    private final MovieMapper movieMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TmdbService(TmdbApiUtil tmdb, MovieMapper movieMapper) {
        this.tmdb = tmdb;
        this.movieMapper = movieMapper;
    }

    public List<Movie> getPopularMovies(int page) {
        String url = tmdb.buildUrl("/movie/popular", "page=" + page);
        JsonNode root = fetch(url);
        if (root == null) return Collections.emptyList();
        return cacheAndReturn(parseMovieList(root.get("results")));
    }

    public List<Movie> searchMovies(String keyword, int page) {
        String url = tmdb.buildUrl("/search/movie", "page=" + page + "&query=" + keyword);
        JsonNode root = fetch(url);
        if (root == null) return Collections.emptyList();
        return cacheAndReturn(parseMovieList(root.get("results")));
    }

    public List<Movie> discoverMovies(String genreIds, String sortBy, String year, int page) {
        StringBuilder params = new StringBuilder("page=" + page);
        if (genreIds != null && !genreIds.isEmpty()) {
            params.append("&with_genres=").append(genreIds);
        }
        if (sortBy != null && !sortBy.isEmpty()) {
            params.append("&sort_by=").append(sortBy);
        }
        if (year != null && !year.isEmpty()) {
            params.append("&primary_release_year=").append(year);
        }
        params.append("&vote_count.gte=50");
        String url = tmdb.buildUrl("/discover/movie", params.toString());
        JsonNode root = fetch(url);
        if (root == null) return Collections.emptyList();

        List<Movie> movies = cacheAndReturn(parseMovieList(root.get("results")));

        // AND 逻辑：多类型同时选中时，只保留包含全部选中类型的电影
        if (genreIds != null && !genreIds.isEmpty() && genreIds.contains(",")) {
            String[] ids = genreIds.split(",");
            movies = movies.stream().filter(m -> {
                String mg = m.getGenres();
                if (mg == null) return false;
                for (String id : ids) {
                    String cn = GENRE_MAP.get(Integer.parseInt(id.trim()));
                    if (cn != null && !mg.contains(cn)) return false;
                }
                return true;
            }).collect(Collectors.toList());
        }
        return movies;
    }

    public Movie getMovieDetail(Long tmdbId) {
        String url = tmdb.buildUrl("/movie/" + tmdbId, "append_to_response=credits");
        JsonNode root = fetch(url);
        if (root == null) return null;

        Movie movie = new Movie();
        movie.setTmdbId(root.get("id").asLong());
        movie.setTitle(getText(root, "title"));
        movie.setOriginalTitle(getText(root, "original_title"));
        movie.setPosterPath(getText(root, "poster_path"));
        movie.setOverview(getText(root, "overview"));
        movie.setVoteAverage(BigDecimal.valueOf(root.path("vote_average").asDouble(0)));
        movie.setVoteCount(root.path("vote_count").asInt(0));
        movie.setRuntime(root.path("runtime").asInt(0));

        String date = getText(root, "release_date");
        if (!date.isEmpty()) movie.setReleaseDate(LocalDate.parse(date));

        // 解析类型（name → 中文名）
        JsonNode genresNode = root.path("genres");
        List<String> genreNames = new ArrayList<>();
        for (JsonNode g : genresNode) {
            String cn = GENRE_MAP.get(g.path("id").asInt());
            if (cn != null) genreNames.add(cn);
        }
        movie.setGenres(String.join(",", genreNames));

        // 解析导演
        JsonNode crew = root.path("credits").path("crew");
        for (JsonNode c : crew) {
            if ("Director".equals(c.path("job").asText())) {
                movie.setDirector(c.path("name").asText());
                break;
            }
        }

        // 解析主演（取前5）
        JsonNode cast = root.path("credits").path("cast");
        List<String> castNames = new ArrayList<>();
        int count = 0;
        for (JsonNode c : cast) {
            if (count++ >= 5) break;
            castNames.add(c.path("name").asText());
        }
        movie.setCast(String.join(",", castNames));

        // 缓存到本地
        Movie existing = movieMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Movie>()
                .eq(Movie::getTmdbId, tmdbId));
        if (existing != null) {
            movie.setId(existing.getId());
            movie.setCreateTime(existing.getCreateTime());
            movieMapper.updateById(movie);
        } else {
            movieMapper.insert(movie);
        }
        return movie;
    }

    public int getPopularTotalPages() {
        String url = tmdb.buildUrl("/movie/popular", "page=1");
        JsonNode root = fetch(url);
        return root != null ? root.path("total_pages").asInt(1) : 1;
    }

    public int getSearchTotalPages(String keyword) {
        String url = tmdb.buildUrl("/search/movie", "page=1&query=" + keyword);
        JsonNode root = fetch(url);
        return root != null ? root.path("total_pages").asInt(1) : 1;
    }

    public int getDiscoverTotalPages(String genreIds, String sortBy, String year) {
        StringBuilder params = new StringBuilder("page=1");
        if (genreIds != null && !genreIds.isEmpty()) params.append("&with_genres=").append(genreIds);
        if (sortBy != null && !sortBy.isEmpty()) params.append("&sort_by=").append(sortBy);
        if (year != null && !year.isEmpty()) params.append("&primary_release_year=").append(year);
        params.append("&vote_count.gte=50");
        String url = tmdb.buildUrl("/discover/movie", params.toString());
        JsonNode root = fetch(url);
        return root != null ? root.path("total_pages").asInt(1) : 1;
    }

    // ---- helpers ----

    private JsonNode fetch(String url) {
        try {
            String json = tmdb.getRestTemplate().getForObject(url, String.class);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("TMDb API 调用失败: {}", url, e);
            return null;
        }
    }

    private List<Movie> parseMovieList(JsonNode results) {
        List<Movie> movies = new ArrayList<>();
        for (JsonNode node : results) {
            Movie m = new Movie();
            m.setTmdbId(node.get("id").asLong());
            m.setTitle(getText(node, "title"));
            m.setOriginalTitle(getText(node, "original_title"));
            m.setPosterPath(getText(node, "poster_path"));
            m.setOverview(getText(node, "overview"));
            m.setVoteAverage(BigDecimal.valueOf(node.path("vote_average").asDouble(0)));
            m.setVoteCount(node.path("vote_count").asInt(0));

            String date = getText(node, "release_date");
            if (!date.isEmpty()) m.setReleaseDate(LocalDate.parse(date));

            // 类型ID → 中文名
            JsonNode genreIds = node.path("genre_ids");
            List<String> names = new ArrayList<>();
            for (JsonNode gid : genreIds) {
                String cn = GENRE_MAP.get(gid.asInt());
                if (cn != null) names.add(cn);
            }
            m.setGenres(String.join(",", names));

            movies.add(m);
        }
        return movies;
    }

    private List<Movie> cacheAndReturn(List<Movie> movies) {
        for (Movie m : movies) {
            Movie existing = movieMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Movie>()
                    .eq(Movie::getTmdbId, m.getTmdbId()));
            if (existing != null) {
                m.setId(existing.getId());
                m.setCreateTime(existing.getCreateTime());
                movieMapper.updateById(m);
            } else {
                movieMapper.insert(m);
            }
        }
        return movies;
    }

    private String getText(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return val == null || val.isNull() ? "" : val.asText();
    }
}
