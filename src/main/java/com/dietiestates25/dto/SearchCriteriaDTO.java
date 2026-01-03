package com.dietiestates25.dto;

import com.dietiestates25.model.EnergyClass;
import com.dietiestates25.model.PropertyType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SearchCriteriaDTO {
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer rooms;
    private PropertyType type;
    private EnergyClass energyClass;

    // Geo-spatial search
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double radiusKm;
}
