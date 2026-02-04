package com.dietiestates25.dto;

import lombok.Data;

@Data
public class AgencyUpdateRequest {
    private String name;
    private String address;
    private String phone;
    private String email;
}
