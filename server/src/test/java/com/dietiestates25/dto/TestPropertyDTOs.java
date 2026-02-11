package com.dietiestates25.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestPropertyDTOs {

    @Test
    void testPropertyUpdateRequest() {
        PropertyUpdateRequest dto = new PropertyUpdateRequest();
        dto.setTitle("Title");
        dto.setDescription("Description");
        dto.setPrice(BigDecimal.TEN);
        dto.setType("Type");
        dto.setCity("City");
        dto.setAddress("Address");
        dto.setRooms(3);
        dto.setFloor(2);
        dto.setBathrooms(1);
        dto.setSizeSqm(100);
        dto.setCondition("Good");
        dto.setYearBuilt(2000);
        dto.setEnergyClass("A");
        dto.setLatitude(BigDecimal.ONE);
        dto.setLongitude(BigDecimal.ZERO);
        dto.setAmenities(Arrays.asList("Wifi", "Pool"));
        dto.setPhotos(Arrays.asList("photo1.jpg"));

        assertEquals("Title", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(BigDecimal.TEN, dto.getPrice());
        assertEquals("Type", dto.getType());
        assertEquals("City", dto.getCity());
        assertEquals("Address", dto.getAddress());
        assertEquals(3, dto.getRooms());
        assertEquals(2, dto.getFloor());
        assertEquals(1, dto.getBathrooms());
        assertEquals(100, dto.getSizeSqm());
        assertEquals("Good", dto.getCondition());
        assertEquals(2000, dto.getYearBuilt());
        assertEquals("A", dto.getEnergyClass());
        assertEquals(BigDecimal.ONE, dto.getLatitude());
        assertEquals(BigDecimal.ZERO, dto.getLongitude());
        assertEquals(2, dto.getAmenities().size());
        assertEquals(1, dto.getPhotos().size());

        PropertyUpdateRequest dto2 = new PropertyUpdateRequest();
        dto2.setTitle("Title");
        dto2.setDescription("Description");
        dto2.setPrice(BigDecimal.TEN);
        dto2.setType("Type");
        dto2.setCity("City");
        dto2.setAddress("Address");
        dto2.setRooms(3);
        dto2.setFloor(2);
        dto2.setBathrooms(1);
        dto2.setSizeSqm(100);
        dto2.setCondition("Good");
        dto2.setYearBuilt(2000);
        dto2.setEnergyClass("A");
        dto2.setLatitude(BigDecimal.ONE);
        dto2.setLongitude(BigDecimal.ZERO);
        dto2.setAmenities(Arrays.asList("Wifi", "Pool"));
        dto2.setPhotos(Arrays.asList("photo1.jpg"));

        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }

    @Test
    void testPropertySearchCriteria() {
        PropertySearchCriteria dto = PropertySearchCriteria.builder()
                .city("City")
                .type("Type")
                .minPrice(BigDecimal.ZERO)
                .maxPrice(BigDecimal.TEN)
                .rooms(2)
                .minSize(50)
                .maxSize(150)
                .floor(1)
                .bathrooms(1)
                .energyClass("A")
                .condition("New")
                .agentEmail("agent@example.com")
                .latitude(10.0)
                .longitude(20.0)
                .radius(5.0)
                .build();

        assertEquals("City", dto.getCity());
        assertEquals("Type", dto.getType());
        assertEquals(BigDecimal.ZERO, dto.getMinPrice());
        assertEquals(BigDecimal.TEN, dto.getMaxPrice());
        assertEquals(2, dto.getRooms());
        assertEquals(50, dto.getMinSize());
        assertEquals(150, dto.getMaxSize());
        assertEquals(1, dto.getFloor());
        assertEquals(1, dto.getBathrooms());
        assertEquals("A", dto.getEnergyClass());
        assertEquals("New", dto.getCondition());
        assertEquals("agent@example.com", dto.getAgentEmail());
        assertEquals(10.0, dto.getLatitude());
        assertEquals(20.0, dto.getLongitude());
        assertEquals(5.0, dto.getRadius());

        PropertySearchCriteria dto2 = new PropertySearchCriteria();
        dto2.setCity("City");
        dto2.setType("Type");
        dto2.setMinPrice(BigDecimal.ZERO);
        dto2.setMaxPrice(BigDecimal.TEN);
        dto2.setRooms(2);
        dto2.setMinSize(50);
        dto2.setMaxSize(150);
        dto2.setFloor(1);
        dto2.setBathrooms(1);
        dto2.setEnergyClass("A");
        dto2.setCondition("New");
        dto2.setAgentEmail("agent@example.com");
        dto2.setLatitude(10.0);
        dto2.setLongitude(20.0);
        dto2.setRadius(5.0);

        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());

        // Test NoArgsConstructor
        PropertySearchCriteria emptyDto = new PropertySearchCriteria();
        assertNull(emptyDto.getCity());
    }
}
