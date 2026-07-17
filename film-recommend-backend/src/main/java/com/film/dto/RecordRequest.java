package com.film.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordRequest {
    @NotNull(message = "电影ID不能为空")
    private Long movieId;
    private Integer rating;  // 1-5
    @Size(max = 500, message = "短评最多500字")
    private String comment;
    @NotNull(message = "状态不能为空")
    private Integer status;  // 1=想看 2=看过
    private LocalDate watchDate;
}
