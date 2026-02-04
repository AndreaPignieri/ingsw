package com.dietiestates25.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgencyCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email; // Agency email

    // Manager Details
    @Email
    @NotBlank
    private String managerEmail;

    @NotBlank
    private String managerFirstName;

    @NotBlank
    private String managerLastName;

    @NotBlank
    private String managerPassword;
}
