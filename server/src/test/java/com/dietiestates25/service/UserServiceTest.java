package com.dietiestates25.service;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- getUser Tests ---

    @Test
    void getUser_ExistingId_ReturnsUserDTO() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        UserDTO result = userService.getUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_NonExistingId_ThrowsException() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userService.getUser(userId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    // --- updateUser Tests ---

    @Test
    void updateUser_ExistingId_ValidUpdate_UpdatesFields() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("OldName");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("NewName");
        // Other fields null

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        userService.updateUser(userId, request);

        // Assert
        assertEquals("NewName", mockUser.getFirstName());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_ExistingId_PartialUpdate_UpdatesOnlyNonNull() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setFirstName("OldFirst");
        mockUser.setLastName("OldLast");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setLastName("NewLast");
        // FirstName is null

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        userService.updateUser(userId, request);

        // Assert
        assertEquals("OldFirst", mockUser.getFirstName()); // Should remain unchanged
        assertEquals("NewLast", mockUser.getLastName()); // Should update
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_NonExistingId_ThrowsException() {
        // Arrange
        Long userId = 99L;
        UserUpdateRequest request = new UserUpdateRequest();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userService.updateUser(userId, request));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
