package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.film.dto.RecordRequest;
import com.film.entity.Movie;
import com.film.entity.WatchRecord;
import com.film.exception.BusinessException;
import com.film.mapper.MovieMapper;
import com.film.mapper.RecordMapper;
import com.film.util.TmdbApiUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {

    private final RecordMapper recordMapper;
    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;
    private final TmdbApiUtil tmdbApiUtil;

    public RecordService(RecordMapper recordMapper, MovieMapper movieMapper,
                         TmdbService tmdbService, TmdbApiUtil tmdbApiUtil) {
        this.recordMapper = recordMapper;
        this.movieMapper = movieMapper;
        this.tmdbService = tmdbService;
        this.tmdbApiUtil = tmdbApiUtil;
    }

    public List<WatchRecord> getMyRecords(Long userId, Integer status) {
        LambdaQueryWrapper<WatchRecord> wrapper = new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId)
            .orderByDesc(WatchRecord::getCreateTime);
        if (status != null) {
            wrapper.eq(WatchRecord::getStatus, status);
        }
        List<WatchRecord> records = recordMapper.selectList(wrapper);
        // 附加 Movie 信息
        for (WatchRecord r : records) {
            enrichMovie(r);
        }
        return records;
    }

    public WatchRecord saveRecord(Long userId, RecordRequest request) {
        Long movieId = resolveMovieId(request);
        if (movieId == null) {
            throw new BusinessException("电影不存在，请提供有效的 movieId 或 tmdbId");
        }
        if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
            throw new BusinessException("评分必须在1-5之间");
        }

        WatchRecord existing = recordMapper.selectOne(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId)
            .eq(WatchRecord::getMovieId, movieId));

        if (existing != null) {
            if (request.getRating() != null) existing.setRating(request.getRating());
            if (request.getComment() != null) existing.setComment(request.getComment());
            if (request.getStatus() != null) existing.setStatus(request.getStatus());
            if (request.getWatchDate() != null) existing.setWatchDate(request.getWatchDate());
            recordMapper.updateById(existing);
            enrichMovie(existing);
            return existing;
        }

        WatchRecord record = new WatchRecord();
        record.setUserId(userId);
        record.setMovieId(movieId);
        record.setRating(request.getRating());
        record.setComment(request.getComment());
        record.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        record.setWatchDate(request.getWatchDate() != null ? request.getWatchDate() : LocalDate.now());
        recordMapper.insert(record);
        enrichMovie(record);
        return record;
    }

    private void enrichMovie(WatchRecord r) {
        Movie m = movieMapper.selectById(r.getMovieId());
        if (m != null) {
            m.setPosterUrl(tmdbApiUtil.getImageUrl(m.getPosterPath(), "w500"));
        }
        r.setMovie(m);
    }

    private Long resolveMovieId(RecordRequest request) {
        if (request.getMovieId() != null) return request.getMovieId();
        if (request.getTmdbId() != null) {
            Movie m = movieMapper.selectOne(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getTmdbId, request.getTmdbId()));
            if (m != null) return m.getId();
            // 本地未缓存，从TMDb拉取
            m = tmdbService.getMovieDetail(request.getTmdbId());
            if (m != null) return m.getId();
        }
        return null;
    }

    public void deleteRecord(Long id, Long userId) {
        WatchRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        recordMapper.deleteById(id);
    }

    public WatchRecord getMyRecordForMovie(Long userId, Long tmdbId) {
        Movie movie = movieMapper.selectOne(new LambdaQueryWrapper<Movie>()
            .eq(Movie::getTmdbId, tmdbId));
        if (movie == null) return null;
        WatchRecord record = recordMapper.selectOne(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId)
            .eq(WatchRecord::getMovieId, movie.getId()));
        if (record != null) {
            movie.setPosterUrl(tmdbApiUtil.getImageUrl(movie.getPosterPath(), "w500"));
            record.setMovie(movie);
        }
        return record;
    }

    public Map<String, Object> getMovieStats(Long tmdbId) {
        Movie movie = movieMapper.selectOne(new LambdaQueryWrapper<Movie>()
            .eq(Movie::getTmdbId, tmdbId));
        if (movie == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("tmdbId", tmdbId);
            empty.put("averageRating", 0.0);
            empty.put("reviewCount", 0);
            return empty;
        }
        List<WatchRecord> records = recordMapper.selectList(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getMovieId, movie.getId())
            .eq(WatchRecord::getStatus, 2)
            .isNotNull(WatchRecord::getRating));

        double avg = records.stream().mapToInt(WatchRecord::getRating).average().orElse(0);
        Map<String, Object> stats = new HashMap<>();
        stats.put("tmdbId", tmdbId);
        stats.put("averageRating", Math.round(avg * 10) / 10.0);
        stats.put("reviewCount", records.size());
        return stats;
    }

    public List<WatchRecord> getMovieReviews(Long tmdbId) {
        Movie movie = movieMapper.selectOne(new LambdaQueryWrapper<Movie>()
            .eq(Movie::getTmdbId, tmdbId));
        if (movie == null) return List.of();
        List<WatchRecord> records = recordMapper.selectList(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getMovieId, movie.getId())
            .eq(WatchRecord::getStatus, 2)
            .isNotNull(WatchRecord::getComment)
            .ne(WatchRecord::getComment, "")
            .orderByDesc(WatchRecord::getCreateTime));
        for (WatchRecord r : records) {
            enrichMovie(r);
        }
        return records;
    }

    public Map<String, Object> getMyStats(Long userId) {
        List<WatchRecord> all = recordMapper.selectList(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId));
        long watched = all.stream().filter(r -> r.getStatus() == 2).count();
        long wishlist = all.stream().filter(r -> r.getStatus() == 1).count();
        double avg = all.stream()
            .filter(r -> r.getStatus() == 2 && r.getRating() != null)
            .mapToInt(WatchRecord::getRating)
            .average().orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("watched", watched);
        stats.put("wishlist", wishlist);
        stats.put("averageRating", Math.round(avg * 10) / 10.0);
        return stats;
    }
}
