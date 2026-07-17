package com.film.controller;

import com.film.dto.RecordRequest;
import com.film.entity.WatchRecord;
import com.film.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ResponseEntity<List<WatchRecord>> getMyRecords(
            Authentication authentication,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.getMyRecords(userId, status));
    }

    @PostMapping
    public ResponseEntity<WatchRecord> saveRecord(Authentication authentication,
                                                   @Valid @RequestBody RecordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.saveRecord(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id,
                                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        recordService.deleteRecord(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/movie/{tmdbId}")
    public ResponseEntity<WatchRecord> getMyRecord(Authentication authentication,
                                                    @PathVariable Long tmdbId) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.getMyRecordForMovie(userId, tmdbId));
    }

    @GetMapping("/movie/{tmdbId}/stats")
    public ResponseEntity<Map<String, Object>> getMovieStats(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(recordService.getMovieStats(tmdbId));
    }

    @GetMapping("/movie/{tmdbId}/reviews")
    public ResponseEntity<List<WatchRecord>> getMovieReviews(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(recordService.getMovieReviews(tmdbId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.getMyStats(userId));
    }
}
