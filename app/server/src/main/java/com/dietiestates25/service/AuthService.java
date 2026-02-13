package com.dietiestates25.service;

import com.dietiestates25.config.JwtService;
import com.dietiestates25.dto.*;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final com.dietiestates25.repository.AgencyRepository agencyRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @org.springframework.transaction.annotation.Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new com.dietiestates25.exception.UserAlreadyExistsException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        user.getRoles().add(com.dietiestates25.model.Role.USER);
        user.setAuthProvider("LOCAL");

        userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public void registerAgency(AgencyRegisterRequest request) {
        if (userRepository.existsByEmail(request.getManagerEmail())) {
            throw new com.dietiestates25.exception.UserAlreadyExistsException("Email already registered");
        }

        // Create Agency
        com.dietiestates25.model.Agency agency = new com.dietiestates25.model.Agency();
        agency.setName(request.getAgencyName());
        agency.setEmail(request.getAgencyEmail());
        agency.setPhone(request.getAgencyPhone());
        agency.setAddress(request.getAgencyAddress());
        agency = agencyRepository.save(agency);

        // Create Manager (User)
        User user = new User();
        user.setEmail(request.getManagerEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getManagerFirstName());
        user.setLastName(request.getManagerLastName());
        user.setAuthProvider("LOCAL");
        user.setAgency(agency);
        user.getRoles().add(com.dietiestates25.model.Role.AGENCY);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.dietiestates25.exception.ResourceNotFoundException("User not found"));

        // Use the authenticated principal directly
        var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setAuthProvider(user.getAuthProvider());
        userDTO.setRole(user.getRoles().stream().findFirst().map(Enum::name).orElse("USER"));

        return new AuthResponse(token, userDTO);
    }
}
