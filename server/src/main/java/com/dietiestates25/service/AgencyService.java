package com.dietiestates25.service;

import com.dietiestates25.dto.AgencyCreateRequest;
import com.dietiestates25.model.Agency;
import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgencyRepository;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAgency(AgencyCreateRequest request) {
        // 1. Create Agency
        Agency agency = new Agency();
        agency.setName(request.getName());
        agency.setAddress(request.getAddress());
        agency.setPhone(request.getPhone());
        agency.setEmail(request.getEmail());

        agency = agencyRepository.save(agency);

        // 2. Create Manager User
        if (userRepository.existsByEmail(request.getManagerEmail())) {
            throw new RuntimeException("User with email " + request.getManagerEmail() + " already exists");
        }

        User manager = new User();
        manager.setEmail(request.getManagerEmail());
        manager.setFirstName(request.getManagerFirstName());
        manager.setLastName(request.getManagerLastName());
        manager.setPasswordHash(passwordEncoder.encode(request.getManagerPassword()));
        manager.setAgency(agency);
        manager.getRoles().add(Role.AGENCY);

        userRepository.save(manager);
    }

    @Transactional(readOnly = true)
    public com.dietiestates25.dto.AgencyDTO getAgencyByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Agency agency = user.getAgency();
        if (agency == null) {
            throw new RuntimeException("User does not belong to an agency");
        }

        return mapToDTO(agency);
    }

    @Transactional
    public void updateAgency(String email, com.dietiestates25.dto.AgencyUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Agency agency = user.getAgency();
        if (agency == null) {
            throw new RuntimeException("User does not belong to an agency");
        }

        if (request.getName() != null)
            agency.setName(request.getName());
        if (request.getAddress() != null)
            agency.setAddress(request.getAddress());
        if (request.getPhone() != null)
            agency.setPhone(request.getPhone());
        if (request.getEmail() != null)
            agency.setEmail(request.getEmail());

        agencyRepository.save(agency);
    }

    private com.dietiestates25.dto.AgencyDTO mapToDTO(Agency agency) {
        com.dietiestates25.dto.AgencyDTO dto = new com.dietiestates25.dto.AgencyDTO();
        dto.setId(agency.getId());
        dto.setName(agency.getName());
        dto.setAddress(agency.getAddress());
        dto.setPhone(agency.getPhone());
        dto.setEmail(agency.getEmail());
        return dto;
    }
}
