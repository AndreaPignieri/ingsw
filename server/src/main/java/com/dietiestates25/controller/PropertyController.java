package com.dietiestates25.controller;

import com.dietiestates25.dto.PropertyCreateRequest;
import com.dietiestates25.dto.PropertyDTO;
import com.dietiestates25.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<Page<PropertyDTO>> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer rooms,
            @RequestParam(required = false) Integer minSize,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer bathrooms,
            @RequestParam(required = false) String energyClass,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String agentEmail,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity
                .ok(propertyService.searchProperties(city, type, minPrice, maxPrice, rooms, minSize, maxSize, floor,
                        bathrooms, energyClass, condition, agentEmail, latitude, longitude, radius, page, limit));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AGENT')")
    public ResponseEntity<PropertyDTO> createProperty(@RequestBody PropertyCreateRequest request,
            java.security.Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyService.createProperty(request, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }
}
