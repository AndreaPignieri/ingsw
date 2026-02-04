package com.dietiestates25.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgencyRegisterRequest {
    @NotBlank
    private String agencyName;

    @Email
    @NotBlank
    private String agencyEmail;

    @NotBlank
    private String agencyPhone;

    @NotBlank
    private String agencyAddress;

    @NotBlank
    private String managerFirstName;

    @NotBlank
    private String managerLastName;

    @Email
    @NotBlank
    private String managerEmail;

    @NotBlank
    private String password;
}
