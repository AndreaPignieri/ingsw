package com.dietiestates25.service;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dietiestates25.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        return mapToDTO(user);
    }

    public UserDTO getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        return mapToDTO(user);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        updateUserFields(user, request);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        updateUserFields(user, request);
        userRepository.save(user);
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());

        updatePassword(user, request);
        updateAgentFields(user, request);
    }

    private void updatePassword(User user, UserUpdateRequest request) {
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new IllegalArgumentException("Old password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Old password does not match");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
    }

    private void updateAgentFields(User user, UserUpdateRequest request) {
        if (user instanceof com.dietiestates25.model.Agent agent) {
            if (request.getBiography() != null) {
                agent.setBiography(request.getBiography());
            }
            if (request.getProfilePhoto() != null) {
                agent.setProfilePhoto(cleanProfilePhotoUrl(request.getProfilePhoto()));
            }
        }
    }

    private String cleanProfilePhotoUrl(String url) {
        if (url != null && url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRoles().stream().findFirst().map(Enum::name).orElse("USER"));
        dto.setAuthProvider(user.getAuthProvider());

        if (user instanceof com.dietiestates25.model.Agent agent) {
            dto.setBiography(agent.getBiography());
            dto.setProfilePhoto(agent.getProfilePhoto());
        }
        return dto;
    }
}
