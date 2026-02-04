package com.dietiestates25.service;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @SuppressWarnings("null")
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    @SuppressWarnings("null")
    public UserDTO getUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    @Transactional
    @SuppressWarnings("null")
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        updateUserFields(user, request);
        userRepository.save(user);
    }

    @Transactional
    @SuppressWarnings("null")
    public void updateUser(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
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
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new IllegalArgumentException("Old password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Old password does not match");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (user instanceof com.dietiestates25.model.Agent) {
            com.dietiestates25.model.Agent agent = (com.dietiestates25.model.Agent) user;
            if (request.getBiography() != null) {
                agent.setBiography(request.getBiography());
            }
            if (request.getProfilePhoto() != null) {
                // Strip any query parameters (timestamp) before saving
                String cleanUrl = request.getProfilePhoto();
                if (cleanUrl.contains("?")) {
                    cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));
                }
                agent.setProfilePhoto(cleanUrl);
            }
        }
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRoles().stream().findFirst().map(Enum::name).orElse("USER"));
        dto.setAuthProvider(user.getAuthProvider());

        if (user instanceof com.dietiestates25.model.Agent) {
            com.dietiestates25.model.Agent agent = (com.dietiestates25.model.Agent) user;
            dto.setBiography(agent.getBiography());
            dto.setProfilePhoto(agent.getProfilePhoto());
        }
        return dto;
    }
}
