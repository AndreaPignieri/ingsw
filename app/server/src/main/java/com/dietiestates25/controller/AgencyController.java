package com.dietiestates25.controller;

import com.dietiestates25.dto.AgencyCreateRequest;
import com.dietiestates25.service.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> createAgency(@RequestBody @Valid AgencyCreateRequest request) {
        agencyService.createAgency(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('AGENCY')")
    public ResponseEntity<com.dietiestates25.dto.AgencyDTO> getMyAgency() {
        String email = ((org.springframework.security.core.userdetails.UserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        return ResponseEntity.ok(agencyService.getAgencyByUser(email));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('AGENCY')")
    public ResponseEntity<Void> updateMyAgency(@RequestBody @Valid com.dietiestates25.dto.AgencyUpdateRequest request) {
        String email = ((org.springframework.security.core.userdetails.UserDetails) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal()).getUsername();
        agencyService.updateAgency(email, request);
        return ResponseEntity.ok().build();
    }
}
