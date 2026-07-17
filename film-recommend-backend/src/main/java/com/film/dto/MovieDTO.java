package com.film.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovieDTO {
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
    private String posterUrl;  // 拼接后的完整图片URL
}
