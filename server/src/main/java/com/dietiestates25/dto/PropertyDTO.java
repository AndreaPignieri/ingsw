package com.dietiestates25.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String type;
    private String city;
    private Integer rooms;
    private Integer floor;
    private Integer bathrooms;
    private Integer sizeSqm;
    private String address;
    private String condition;
    private Integer yearBuilt;
    private String energyClass;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> amenities;
    private List<String> photos;

    // Agent/Agency Details
    private Long agentId;
    private String agentName;
    private String agentEmail;
    private String agentPhone;
    private String agencyName;
}
