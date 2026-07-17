package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.film.dto.RecordRequest;
import com.film.entity.WatchRecord;
import com.film.exception.BusinessException;
import com.film.mapper.RecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecordService {

    private final RecordMapper recordMapper;

    public RecordService(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    public List<WatchRecord> getMyRecords(Long userId) {
        return recordMapper.selectList(
            new LambdaQueryWrapper<WatchRecord>()
                .eq(WatchRecord::getUserId, userId)
                .orderByDesc(WatchRecord::getCreateTime)
        );
    }

    public WatchRecord createRecord(Long userId, RecordRequest request) {
        boolean exists = recordMapper.exists(new LambdaQueryWrapper<WatchRecord>()
            .eq(WatchRecord::getUserId, userId)
            .eq(WatchRecord::getMovieId, request.getMovieId()));
        if (exists) {
            throw new BusinessException("该电影已有记录，请直接编辑");
        }
        WatchRecord record = new WatchRecord();
        record.setUserId(userId);
        record.setMovieId(request.getMovieId());
        record.setRating(request.getRating());
        record.setComment(request.getComment());
        record.setStatus(request.getStatus());
        record.setWatchDate(request.getWatchDate() != null ? request.getWatchDate() : LocalDate.now());
        recordMapper.insert(record);
        return record;
    }

    public WatchRecord updateRecord(Long id, Long userId, RecordRequest request) {
        WatchRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        if (request.getRating() != null) record.setRating(request.getRating());
        if (request.getComment() != null) record.setComment(request.getComment());
        if (request.getStatus() != null) record.setStatus(request.getStatus());
        recordMapper.updateById(record);
        return record;
    }

    public void deleteRecord(Long id, Long userId) {
        WatchRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        recordMapper.deleteById(id);
    }
}
