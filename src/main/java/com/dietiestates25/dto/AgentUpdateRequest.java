package com.dietiestates25.dto;

import lombok.Data;

@Data
public class AgentUpdateRequest {
    private String firstName;
    private String lastName;
    private String biography;
    private String profilePhoto;
}
