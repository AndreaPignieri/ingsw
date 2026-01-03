package com.dietiestates25.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyCreateRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private BigDecimal price;
    @NotBlank
    private String type;
    private String city;
    private Integer rooms;
    private Integer floor;
    private String energyClass;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> amenities;
    private List<String> photos;
}
