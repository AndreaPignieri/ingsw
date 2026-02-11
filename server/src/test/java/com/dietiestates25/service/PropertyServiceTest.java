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

    @Mock
    private com.dietiestates25.repository.AgentRepository agentRepository;

    @InjectMocks
    private PropertyService propertyService;

    // --- Search Tests ---

    @Test
    void searchProperties_AllFilters_CallsRepo() {
        Page<Property> emptyPage = new PageImpl<>(Collections.emptyList());
        org.mockito.ArgumentCaptor<Specification<Property>> specCaptor = org.mockito.ArgumentCaptor
                .forClass(Specification.class);

        when(propertyRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(emptyPage);

        com.dietiestates25.dto.PropertySearchCriteria criteria = com.dietiestates25.dto.PropertySearchCriteria.builder()
                .city("Rome")
                .type("SALE")
                .minPrice(BigDecimal.valueOf(100))
                .maxPrice(BigDecimal.valueOf(1000))
                .rooms(2)
                .minSize(50)
                .maxSize(100)
                .floor(1)
                .bathrooms(1)
                .energyClass("A1")
                .condition("Good")
                .agentEmail("agent@test.com")
                .build();

        Page<PropertyDTO> result = propertyService.searchProperties(criteria, 0, 10);

        assertNotNull(result);
        verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));

        // Execute the specification to cover the lambda
        Specification<Property> spec = specCaptor.getValue();
        assertNotNull(spec);

        jakarta.persistence.criteria.Root<Property> root = mock(jakarta.persistence.criteria.Root.class);
        jakarta.persistence.criteria.CriteriaQuery<?> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate mockPredicate = mock(jakarta.persistence.criteria.Predicate.class);
        jakarta.persistence.criteria.Join<Object, Object> mockJoin = mock(jakarta.persistence.criteria.Join.class);
        jakarta.persistence.criteria.Path<Object> mockPath = mock(jakarta.persistence.criteria.Path.class);

        // Mock generic return for any cb method to avoid NPEs
        lenient().when(cb.like(any(), anyString())).thenReturn(mockPredicate);
        lenient().when(cb.equal(any(), any())).thenReturn(mockPredicate);
        lenient()
                .when(cb.greaterThanOrEqualTo(any(jakarta.persistence.criteria.Expression.class),
                        any(Comparable.class)))
                .thenReturn(mockPredicate);
        lenient().when(cb.lessThanOrEqualTo(any(jakarta.persistence.criteria.Expression.class), any(Comparable.class)))
                .thenReturn(mockPredicate);
        lenient().when(cb.and(any())).thenReturn(mockPredicate);

        // Mock root behavior
        lenient().when(root.get(anyString())).thenReturn(mockPath);
        lenient().when(root.join("agent")).thenReturn(mockJoin);
        lenient().when(mockJoin.get("email")).thenReturn(mockPath);

        spec.toPredicate(root, query, cb);
    }

    @Test
    void searchProperties_GeoFilter_CallsRepo() {
        Page<Property> emptyPage = new PageImpl<>(Collections.emptyList());
        org.mockito.ArgumentCaptor<Specification<Property>> specCaptor = org.mockito.ArgumentCaptor
                .forClass(Specification.class);

        when(propertyRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(emptyPage);

        com.dietiestates25.dto.PropertySearchCriteria criteria = com.dietiestates25.dto.PropertySearchCriteria.builder()
                .latitude(40.85)
                .longitude(14.26)
                .radius(10.0)
                .build();

        Page<PropertyDTO> result = propertyService.searchProperties(criteria, 0, 10);

        assertNotNull(result);
        verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));

        Specification<Property> spec = specCaptor.getValue();

        jakarta.persistence.criteria.Root<Property> root = mock(jakarta.persistence.criteria.Root.class);
        jakarta.persistence.criteria.CriteriaQuery<?> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate mockPredicate = mock(jakarta.persistence.criteria.Predicate.class);
        jakarta.persistence.criteria.Path<Object> mockPath = mock(jakarta.persistence.criteria.Path.class);
        jakarta.persistence.criteria.Expression<Double> mockExpr = mock(jakarta.persistence.criteria.Expression.class);

        lenient().when(cb.lessThan(any(), any(Double.class))).thenReturn(mockPredicate); // For radius check
        lenient().when(cb.and(any())).thenReturn(mockPredicate);
        lenient().when(root.get("location")).thenReturn(mockPath);

        // Fix generic type inference for creating function return stub
        jakarta.persistence.criteria.Expression mockFunctionResult = mock(
                jakarta.persistence.criteria.Expression.class);
        lenient().doReturn(mockFunctionResult).when(cb).function(eq("ST_Distance"), eq(Double.class), any(), any());

        lenient().when(cb.literal(any())).thenReturn((jakarta.persistence.criteria.Expression) mockExpr);

        spec.toPredicate(root, query, cb);
    }

    @Test
    void createProperty_ValidData_SavesProperty() {
        PropertyCreateRequest request = new PropertyCreateRequest();
        request.setTitle("Villa");
        request.setType("SALE");
        request.setEnergyClass("A1");
        request.setCity("Napoli");
        request.setPrice(BigDecimal.valueOf(1000));
        request.setLatitude(BigDecimal.valueOf(40.85));
        request.setLongitude(BigDecimal.valueOf(14.26));
        request.setAmenities(Collections.singletonList("WiFi"));
        request.setPhotos(Collections.singletonList("http://photo.url"));

        String agentEmail = "agent@test.com";
        com.dietiestates25.model.Agent mockAgent = new com.dietiestates25.model.Agent();
        mockAgent.setEmail(agentEmail);
        com.dietiestates25.model.Agency mockAgency = new com.dietiestates25.model.Agency();
        mockAgency.setName("Test Agency"); // Set name for DTO mapping
        mockAgent.setAgency(mockAgency);

        com.dietiestates25.model.Amenity mockAmenity = new com.dietiestates25.model.Amenity();
        mockAmenity.setName("WiFi");

        when(agentRepository.findByEmail(agentEmail)).thenReturn(Optional.of(mockAgent));
        when(amenityRepository.findByName("WiFi")).thenReturn(Optional.of(mockAmenity));

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
        assertEquals("WiFi", result.getAmenities().get(0));
        assertEquals("http://photo.url", result.getPhotos().get(0));
        assertEquals("Test Agency", result.getAgencyName());
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

    @Test
    void createProperty_AgentNotFound_ThrowsException() {
        PropertyCreateRequest request = new PropertyCreateRequest();
        String agentEmail = "unknown@test.com";

        when(agentRepository.findByEmail(agentEmail)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> propertyService.createProperty(request, agentEmail));
    }

    @Test
    void createProperty_AgentNoAgency_ThrowsException() {
        PropertyCreateRequest request = new PropertyCreateRequest();
        String agentEmail = "agent@test.com";
        com.dietiestates25.model.Agent mockAgent = new com.dietiestates25.model.Agent();
        mockAgent.setEmail(agentEmail);
        mockAgent.setAgency(null); // No agency

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
        p.setType(PropertyType.SALE);
        p.setCity("Rome");
        p.setEnergyClass(com.dietiestates25.model.EnergyClass.A1);

        com.dietiestates25.model.Agent mockAgent = new com.dietiestates25.model.Agent();
        mockAgent.setId(10L);
        mockAgent.setFirstName("John");
        mockAgent.setLastName("Doe");
        mockAgent.setEmail("john@doe.com");
        p.setAgent(mockAgent);

        com.dietiestates25.model.Agency mockAgency = new com.dietiestates25.model.Agency();
        mockAgency.setName("Agency A");
        p.setAgency(mockAgency);

        org.locationtech.jts.geom.Point mockPoint = new org.locationtech.jts.geom.GeometryFactory()
                .createPoint(new org.locationtech.jts.geom.Coordinate(10, 20));
        p.setLocation(mockPoint);

        when(propertyRepository.findById(id)).thenReturn(Optional.of(p));

        PropertyDTO result = propertyService.getProperty(id);

        assertEquals("Test", result.getTitle());
        assertEquals("A1", result.getEnergyClass());
        assertEquals("John Doe", result.getAgentName());
        assertEquals("Agency A", result.getAgencyName());
        assertEquals(BigDecimal.valueOf(20.0), result.getLatitude());
        assertEquals(BigDecimal.valueOf(10.0), result.getLongitude());
    }

    @Test
    void getProperty_NonExistingId_ThrowsException() {
        Long id = 99L;
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> propertyService.getProperty(id));
    }
}
