package com.dietiestates25.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyUpdateRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private String type;
    private String city;
    private String address;
    private Integer rooms;
    private Integer floor;
    private Integer bathrooms;
    private Integer sizeSqm;
    private String condition;
    private Integer yearBuilt;
    private String energyClass;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> amenities;
    private List<String> photos;
}
