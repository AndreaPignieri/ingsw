package com.dietiestates25.service;

import com.dietiestates25.dto.PropertyCreateRequest;
import com.dietiestates25.dto.PropertyDTO;
import com.dietiestates25.model.Property;
import com.dietiestates25.model.PropertyType;
import com.dietiestates25.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private com.dietiestates25.repository.AmenityRepository amenityRepository;

    @InjectMocks
    private PropertyService propertyService;

    // --- Search Tests ---

    @Test
    void searchProperties_CallsRepo() {
        Page<Property> emptyPage = new PageImpl<>(Collections.emptyList());
        when(propertyRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Property>>any(),
                any(Pageable.class))).thenReturn(emptyPage);

        com.dietiestates25.dto.PropertySearchCriteria criteria = com.dietiestates25.dto.PropertySearchCriteria.builder()
                .city("Rome")
                .build();

        Page<PropertyDTO> result = propertyService.searchProperties(criteria, 0, 10);

        assertNotNull(result);
        verify(propertyRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Property>>any(),
                any(Pageable.class));
    }

    // --- Create Tests ---

    @Mock
    private com.dietiestates25.repository.AgentRepository agentRepository;

    @Test
    void createProperty_ValidData_SavesProperty() {
        PropertyCreateRequest request = new PropertyCreateRequest();
        request.setTitle("Villa");
        request.setType("SALE"); // Assuming PropertyType.SALE exists
        request.setEnergyClass("A1");
        request.setCity("Napoli");
        request.setPrice(BigDecimal.valueOf(1000));
        request.setLatitude(BigDecimal.valueOf(40.85));
        request.setLongitude(BigDecimal.valueOf(14.26));

        String agentEmail = "agent@test.com";
        com.dietiestates25.model.Agent mockAgent = new com.dietiestates25.model.Agent();
        mockAgent.setEmail(agentEmail);
        com.dietiestates25.model.Agency mockAgency = new com.dietiestates25.model.Agency();
        mockAgent.setAgency(mockAgency);

        when(agentRepository.findByEmail(agentEmail)).thenReturn(Optional.of(mockAgent));

        when(propertyRepository.save(any(Property.class))).thenAnswer(i -> {
            Property p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PropertyDTO result = propertyService.createProperty(request, agentEmail);

        assertNotNull(result);
        assertEquals("Villa", result.getTitle());
        assertEquals("SALE", result.getType());
        assertEquals("A1", result.getEnergyClass());
    }

    @Test
    void createProperty_InvalidType_ThrowsException() {
        PropertyCreateRequest request = new PropertyCreateRequest();
        request.setType("INVALID_TYPE");
        request.setTitle("Fail");
        String agentEmail = "agent@test.com";

        com.dietiestates25.model.Agent mockAgent = new com.dietiestates25.model.Agent();
        mockAgent.setEmail(agentEmail);
        com.dietiestates25.model.Agency mockAgency = new com.dietiestates25.model.Agency();
        mockAgent.setAgency(mockAgency);

        when(agentRepository.findByEmail(agentEmail)).thenReturn(Optional.of(mockAgent));

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(request, agentEmail));
    }

    // --- Get Tests ---

    @Test
    void getProperty_ExistingId_ReturnsDTO() {
        Long id = 1L;
        Property p = new Property();
        p.setId(id);
        p.setTitle("Test");
        p.setType(PropertyType.SALE); // Needed for mapping
        p.setCity("Rome");

        when(propertyRepository.findById(id)).thenReturn(Optional.of(p));

        PropertyDTO result = propertyService.getProperty(id);

        assertEquals("Test", result.getTitle());
    }

    @Test
    void getProperty_NonExistingId_ThrowsException() {
        Long id = 99L;
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> propertyService.getProperty(id));
    }
}
