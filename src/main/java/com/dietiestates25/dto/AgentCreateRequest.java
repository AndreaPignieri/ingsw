package com.dietiestates25.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentCreateRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String biography;
    private String profilePhoto;
}
