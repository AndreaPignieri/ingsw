package com.dietiestates25.service;

import com.dietiestates25.dto.PropertyCreateRequest;
import com.dietiestates25.dto.PropertyDTO;
import com.dietiestates25.model.Property;
import com.dietiestates25.model.Amenity;
import com.dietiestates25.model.PropertyPhoto;
import com.dietiestates25.repository.AmenityRepository;
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

import com.dietiestates25.model.Agent;
import com.dietiestates25.model.EnergyClass;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.exception.ResourceNotFoundException;
import org.locationtech.jts.geom.Coordinate;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final AgentRepository agentRepository;
    private final org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(
            new org.locationtech.jts.geom.PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public Page<PropertyDTO> searchProperties(com.dietiestates25.dto.PropertySearchCriteria criteria, int page,
            int limit) {
        Specification<Property> spec = createSearchSpecification(criteria);
        Page<Property> result = propertyRepository.findAll(spec, PageRequest.of(page, limit));
        return result.map(this::mapToDTO);
    }

    private Specification<Property> createSearchSpecification(com.dietiestates25.dto.PropertySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getCity() != null && !criteria.getCity().isEmpty())
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + criteria.getCity().toLowerCase() + "%"));
            if (criteria.getType() != null && !criteria.getType().isEmpty())
                predicates.add(cb.equal(root.get("type"),
                        com.dietiestates25.model.PropertyType.valueOf(criteria.getType().toUpperCase())));
            if (criteria.getMinPrice() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            if (criteria.getMaxPrice() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            if (criteria.getRooms() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("rooms"), criteria.getRooms()));
            if (criteria.getMinSize() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("sizeSqm"), criteria.getMinSize()));
            if (criteria.getMaxSize() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("sizeSqm"), criteria.getMaxSize()));
            if (criteria.getFloor() != null)
                predicates.add(cb.equal(root.get("floor"), criteria.getFloor()));
            if (criteria.getBathrooms() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("bathrooms"), criteria.getBathrooms()));
            if (criteria.getEnergyClass() != null && !criteria.getEnergyClass().isEmpty())
                predicates.add(cb.equal(root.get("energyClass"),
                        EnergyClass.valueOf(criteria.getEnergyClass().toUpperCase())));
            if (criteria.getCondition() != null && !criteria.getCondition().isEmpty())
                predicates.add(cb.equal(root.get("condition"), criteria.getCondition()));
            if (criteria.getAgentEmail() != null && !criteria.getAgentEmail().isEmpty()) {
                predicates.add(cb.equal(root.join("agent").get("email"), criteria.getAgentEmail()));
            }
            if (criteria.getLatitude() != null && criteria.getLongitude() != null && criteria.getRadius() != null) {
                var point = geometryFactory
                        .createPoint(new Coordinate(criteria.getLongitude(), criteria.getLatitude()));
                predicates.add(cb.lessThan(
                        cb.function("ST_Distance", Double.class, root.get("location"), cb.literal(point)),
                        criteria.getRadius()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public PropertyDTO createProperty(PropertyCreateRequest request, String agentEmail) {
        Agent agent = agentRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with email: " + agentEmail));

        if (agent.getAgency() == null) {
            throw new ResourceNotFoundException("Agent does not belong to an agency");
        }

        Property property = mapRequestToEntity(request, agent);
        propertyRepository.save(property);

        return mapToDTO(property);
    }

    private Property mapRequestToEntity(PropertyCreateRequest request, Agent agent) {
        Property property = new Property();
        property.setAgent(agent);
        property.setAgency(agent.getAgency());

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        try {
            property.setType(com.dietiestates25.model.PropertyType.valueOf(request.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid property type");
        }
        property.setCity(request.getCity());
        property.setRooms(request.getRooms());
        property.setFloor(request.getFloor());
        property.setAddress(request.getAddress());
        property.setBathrooms(request.getBathrooms());
        property.setCondition(request.getCondition());
        property.setYearBuilt(request.getYearBuilt());
        property.setSizeSqm(request.getSizeSqm());

        if (request.getEnergyClass() != null && !request.getEnergyClass().isEmpty()) {
            property.setEnergyClass(EnergyClass.valueOf(request.getEnergyClass().toUpperCase()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            property.setLocation(geometryFactory.createPoint(new Coordinate(
                    request.getLongitude().doubleValue(), request.getLatitude().doubleValue())));
        }

        addAmenities(property, request.getAmenities());
        addPhotos(property, request.getPhotos());

        return property;
    }

    private void addAmenities(Property property, List<String> amenities) {
        if (amenities != null) {
            for (String amenityName : amenities) {
                Amenity amenity = amenityRepository.findByName(amenityName)
                        .orElseGet(() -> {
                            Amenity newAmenity = new Amenity();
                            newAmenity.setName(amenityName);
                            return amenityRepository.save(newAmenity);
                        });
                property.getAmenities().add(amenity);
            }
        }
    }

    private void addPhotos(Property property, List<String> photos) {
        if (photos != null) {
            for (String photoUrl : photos) {
                PropertyPhoto photo = new PropertyPhoto();
                photo.setUrl(photoUrl);
                photo.setProperty(property);
                property.getPhotos().add(photo);
            }
        }
    }

    public PropertyDTO getProperty(Long id) {
        return propertyRepository.findById(id).map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));
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
        dto.setAddress(p.getAddress());
        dto.setBathrooms(p.getBathrooms());
        dto.setCondition(p.getCondition());
        dto.setYearBuilt(p.getYearBuilt());
        dto.setSizeSqm(p.getSizeSqm());

        if (p.getAmenities() != null) {
            dto.setAmenities(p.getAmenities().stream().map(com.dietiestates25.model.Amenity::getName).toList());
        }
        if (p.getPhotos() != null) {
            dto.setPhotos(p.getPhotos().stream().map(com.dietiestates25.model.PropertyPhoto::getUrl).toList());
        }

        if (p.getAgent() != null) {
            dto.setAgentId(p.getAgent().getId());
            dto.setAgentName(p.getAgent().getFirstName() + " " + p.getAgent().getLastName());
            dto.setAgentEmail(p.getAgent().getEmail());
            dto.setAgentPhone(p.getAgent().getPhoneNumber()); // Assuming Agent has phone, check Model later
        }

        if (p.getAgency() != null) {
            dto.setAgencyName(p.getAgency().getName());
        }

        return dto;
    }
}
