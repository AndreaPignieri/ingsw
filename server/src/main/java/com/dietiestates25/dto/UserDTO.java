package com.dietiestates25.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String authProvider;
    private String biography;
    private String profilePhoto;
}
