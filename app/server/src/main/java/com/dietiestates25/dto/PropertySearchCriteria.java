package com.dietiestates25.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySearchCriteria {
    private String city;
    private String type;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer rooms;
    private Integer minSize;
    private Integer maxSize;
    private Integer floor;
    private Integer bathrooms;
    private String energyClass;
    private String condition;
    private String agentEmail;
    private Double latitude;
    private Double longitude;
    private Double radius;
}
