package com.film.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("movie")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tmdbId;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private LocalDate releaseDate;
    private BigDecimal voteAverage;
    private Integer voteCount;
    private Integer runtime;
    private String genres;
    private String director;
    private String cast;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String posterUrl;
}
