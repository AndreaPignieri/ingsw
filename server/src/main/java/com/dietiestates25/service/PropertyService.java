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

    public Page<PropertyDTO> searchProperties(String city, String type, BigDecimal minPrice, BigDecimal maxPrice,
            Integer rooms, Integer minSize, Integer maxSize, Integer floor, Integer bathrooms, String energyClass,
            String condition, String agentEmail, Double latitude, Double longitude, Double radius,
            int page, int limit) {
        System.out.println(
                "DEBUG: searchProperties called with city=" + city + ", type=" + type + ", agentEmail=" + agentEmail);
        Specification<Property> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (city != null && !city.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")); // Changed to fuzzy
                                                                                                     // search for
                                                                                                     // better UX
            if (type != null && !type.isEmpty())
                predicates.add(
                        cb.equal(root.get("type"), com.dietiestates25.model.PropertyType.valueOf(type.toUpperCase())));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (rooms != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("rooms"), rooms)); // Changed to x+
            if (minSize != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("sizeSqm"), minSize));
            if (maxSize != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("sizeSqm"), maxSize));
            if (floor != null)
                predicates.add(cb.equal(root.get("floor"), floor));
            if (bathrooms != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("bathrooms"), bathrooms)); // Assuming x+ for bathrooms
                                                                                           // too
            if (energyClass != null && !energyClass.isEmpty())
                predicates.add(cb.equal(root.get("energyClass"), EnergyClass.valueOf(energyClass.toUpperCase())));
            if (condition != null && !condition.isEmpty())
                predicates.add(cb.equal(root.get("condition"), condition));

            if (agentEmail != null && !agentEmail.isEmpty()) {
                // Join with Agent (User) to filter by email
                // property.agent -> User.email
                predicates.add(cb.equal(root.join("agent").get("email"), agentEmail));
            }

            if (latitude != null && longitude != null && radius != null) {
                var point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                // Use Hibernate Spatial's ST_Distance function
                // Note: The database column is 'geography', so ST_Distance returns meters.
                // We assume 'radius' is passed in meters.
                predicates.add(cb.lessThan(
                        cb.function("ST_Distance", Double.class, root.get("location"), cb.literal(point)),
                        radius));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Property> result = propertyRepository.findAll(spec, PageRequest.of(page, limit));
        System.out.println("DEBUG: searchProperties found " + result.getTotalElements() + " items.");
        return result.map(this::mapToDTO);
    }

    @Transactional
    public PropertyDTO createProperty(PropertyCreateRequest request, String agentEmail) {
        Agent agent = agentRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with email: " + agentEmail));

        // Agent must have an agency
        if (agent.getAgency() == null) {
            throw new RuntimeException("Agent does not belong to an agency");
        }

        Property property = new Property();
        property.setAgent(agent);
        property.setAgency(agent.getAgency());

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

        if (request.getAmenities() != null) {
            for (String amenityName : request.getAmenities()) {
                Amenity amenity = amenityRepository.findByName(amenityName)
                        .orElseGet(() -> {
                            Amenity newAmenity = new Amenity();
                            newAmenity.setName(amenityName);
                            return amenityRepository.save(newAmenity);
                        });
                property.getAmenities().add(amenity);
            }
        }

        if (request.getPhotos() != null) {
            for (String photoUrl : request.getPhotos()) {
                PropertyPhoto photo = new PropertyPhoto();
                photo.setUrl(photoUrl);
                photo.setProperty(property);
                property.getPhotos().add(photo);
            }
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
