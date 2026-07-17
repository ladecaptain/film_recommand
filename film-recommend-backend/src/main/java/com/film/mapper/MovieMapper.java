package com.film.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.film.entity.Movie;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
}
