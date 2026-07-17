package com.film.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordRequest {
    private Long movieId;
    private Long tmdbId;
    private Integer rating;
    @Size(max = 500, message = "短评最多500字")
    private String comment;
    private Integer status;
    private LocalDate watchDate;
}
