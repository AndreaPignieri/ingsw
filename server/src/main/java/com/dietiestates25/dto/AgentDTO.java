package com.dietiestates25.dto;

import lombok.Data;

@Data
public class AgentDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String biography;
    private String profilePhoto;
    private java.time.LocalDate birthDate;
    private String phoneNumber;
    private String agencyName;
    private java.util.List<PropertyDTO> properties;
}
