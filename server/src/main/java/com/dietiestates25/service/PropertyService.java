package com.dietiestates25.service;

import com.dietiestates25.dto.PropertyCreateRequest;
import com.dietiestates25.dto.PropertyDTO;
import com.dietiestates25.model.Property;
import com.dietiestates25.repository.PropertyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(
            new org.locationtech.jts.geom.PrecisionModel(), 4326);

    public Page<PropertyDTO> searchProperties(String city, BigDecimal minPrice, BigDecimal maxPrice, Integer rooms,
            int page, int limit) {
        Specification<Property> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (city != null)
                predicates.add(cb.equal(root.get("city"), city));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (rooms != null)
                predicates.add(cb.equal(root.get("rooms"), rooms));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return propertyRepository.findAll(spec, PageRequest.of(page, limit))
                .map(this::mapToDTO);
    }

    @Transactional
    public PropertyDTO createProperty(PropertyCreateRequest request) {
        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        try {
            property.setType(com.dietiestates25.model.PropertyType.valueOf(request.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid property type");
        }
        property.setCity(request.getCity());
        property.setRooms(request.getRooms());
        property.setFloor(request.getFloor());
        if (request.getEnergyClass() != null) {
            property.setEnergyClass(
                    com.dietiestates25.model.EnergyClass.valueOf(request.getEnergyClass().toUpperCase()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            property.setLocation(geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(
                    request.getLongitude().doubleValue(), request.getLatitude().doubleValue())));
        }

        propertyRepository.save(property);
        return mapToDTO(property);
    }

    @SuppressWarnings("null")
    public PropertyDTO getProperty(Long id) {
        return propertyRepository.findById(id).map(this::mapToDTO).orElseThrow(() -> new RuntimeException("Not found"));
    }

    private PropertyDTO mapToDTO(Property p) {
        PropertyDTO dto = new PropertyDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setType(p.getType().name());
        dto.setCity(p.getCity());
        dto.setRooms(p.getRooms());
        dto.setFloor(p.getFloor());
        if (p.getEnergyClass() != null)
            dto.setEnergyClass(p.getEnergyClass().name());

        if (p.getLocation() != null) {
            dto.setLatitude(BigDecimal.valueOf(p.getLocation().getY()));
            dto.setLongitude(BigDecimal.valueOf(p.getLocation().getX()));
        }
        return dto;
    }
}
