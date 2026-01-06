package com.dietiestates25.service;

import com.dietiestates25.config.JwtService;
import com.dietiestates25.dto.AuthResponse;
import com.dietiestates25.dto.LoginRequest;
import com.dietiestates25.dto.RegisterRequest;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // --- register Tests ---

    @Test
    void register_NewEmail_SavesUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).save(argThat(user -> user.getEmail().equals("new@example.com") &&
                user.getRoles().contains(com.dietiestates25.model.Role.USER)));
    }

    @Test
    void register_ExistingEmail_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // --- login Tests ---

    @Test
    void login_ValidCredentials_ReturnsToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");
        mockUser.setFirstName("First");
        mockUser.setLastName("Last");
        mockUser.setPasswordHash("encodedPass");

        // Mock Authentication and UserDetails
        org.springframework.security.core.Authentication mockAuth = mock(
                org.springframework.security.core.Authentication.class);
        org.springframework.security.core.userdetails.UserDetails mockDetails = mock(
                org.springframework.security.core.userdetails.UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(mockDetails);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockDetails)).thenReturn("mockToken");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("user@example.com", response.getUser().getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad creds"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        // repository lookup shouldn't be reached if auth manager fails first?
        // Actually implementation calls auth manager first.
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password");

        // Auth manager might indicate success if it was just checking logic, but
        // usually it checks DB.
        // In this architecture, if auth manager passes (maybe using a different
        // provider?), but we check DB again.
        // Assuming AuthManager mocks success:

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User not found", exception.getMessage());
    }
}
