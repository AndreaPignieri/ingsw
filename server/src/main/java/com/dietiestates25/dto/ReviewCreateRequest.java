package com.dietiestates25.dto;

import lombok.Data;

@Data
public class ReviewCreateRequest {
    private Integer score;
    private String comment;
    private Long agentId;
}
