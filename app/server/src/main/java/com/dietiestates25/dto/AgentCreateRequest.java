package com.dietiestates25.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentCreateRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @jakarta.validation.constraints.Email
    private String email;
    @NotBlank
    private String password;
    private String biography;
    private String profilePhoto;
}
