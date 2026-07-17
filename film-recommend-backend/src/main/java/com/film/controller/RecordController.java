package com.film.controller;

import com.film.dto.RecordRequest;
import com.film.entity.WatchRecord;
import com.film.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ResponseEntity<List<WatchRecord>> getMyRecords(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.getMyRecords(userId));
    }

    @PostMapping
    public ResponseEntity<WatchRecord> createRecord(Authentication authentication,
                                                     @Valid @RequestBody RecordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.createRecord(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WatchRecord> updateRecord(@PathVariable Long id,
                                                     Authentication authentication,
                                                     @RequestBody RecordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(recordService.updateRecord(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id,
                                              Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        recordService.deleteRecord(id, userId);
        return ResponseEntity.ok().build();
    }
}
