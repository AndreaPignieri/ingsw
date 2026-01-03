package com.dietiestates25.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationDTO {
    private Long id;
    private Integer score;
    private String comment;
    private LocalDateTime date;
}
