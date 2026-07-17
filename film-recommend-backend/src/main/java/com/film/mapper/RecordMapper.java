package com.film.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.film.entity.WatchRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecordMapper extends BaseMapper<WatchRecord> {
}
