package com.dietiestates25.service;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.exception.ResourceNotFoundException;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // --- getUser Tests ---

    @Test
    void getUser_ExistingId_ReturnsUserDTO() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUser_ExistingEmail_ReturnsUserDTO() {
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.getUser(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getUser_NonExistingId_ThrowsException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void getUser_NonExistingEmail_ThrowsException() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(email));
    }

    // --- updateUser Tests ---

    @Test
    void updateUser_ById_ValidUpdate_UpdatesFields() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("Old");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("New");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.updateUser(userId, request);

        assertEquals("New", mockUser.getFirstName());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_ByEmail_ValidUpdate_UpdatesFields() {
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setLastName("NewLast");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        userService.updateUser(email, request);

        assertEquals("NewLast", mockUser.getLastName());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_PasswordUpdate_Success() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPasswordHash("oldHash");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setOldPassword("oldPass");
        request.setPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");

        userService.updateUser(userId, request);

        assertEquals("newHash", mockUser.getPasswordHash());
    }

    @Test
    void updateUser_PasswordUpdate_MissingOldPassword_ThrowsException() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("newPass");
        // oldPassword is null

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    void updateUser_PasswordUpdate_WrongOldPassword_ThrowsException() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setPasswordHash("oldHash");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setOldPassword("wrongPass");
        request.setPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPass", "oldHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    void updateUser_AgentFields_UpdateBiographyAndPhoto() {
        Long userId = 1L;
        Agent mockAgent = new Agent();
        mockAgent.setId(userId);
        mockAgent.setBiography("Old Bio");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setBiography("New Bio");
        request.setProfilePhoto("http://example.com/photo.jpg?query=param");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockAgent));

        userService.updateUser(userId, request);

        assertEquals("New Bio", mockAgent.getBiography());
        assertEquals("http://example.com/photo.jpg", mockAgent.getProfilePhoto()); // Cleaned URL
    }

    @Test
    void mapToDTO_UserFields_MappedCorrectly() {
        // Indirectly tested via getUser, but verifying Agent specific mapping
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);
        agent.setEmail("agent@test.com");
        agent.setBiography("Bio");
        agent.getRoles().add(Role.AGENT);

        when(userRepository.findById(id)).thenReturn(Optional.of(agent));

        UserDTO dto = userService.getUser(id);

        assertEquals("Bio", dto.getBiography());
        assertEquals("AGENT", dto.getRole());
    }
}
