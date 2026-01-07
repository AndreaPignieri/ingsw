package com.dietiestates25.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;
    private String comment;
}
