package com.dietiestates25.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    // 256-bit key for HMAC-SHA256 (32 bytes encoded in Base64)
    private final String SECRET_KEY = "NDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI0MjQyNDI=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        userDetails = new User("testuser", "password", Collections.emptyList());
    }

    @Test
    void generateToken_ExtractUsername_Success() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_InvalidUsername_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void generateToken_WithExtraClaims_Success() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        String token = jwtService.generateToken(claims, userDetails);

        // We can't easily extract custom claims with the public methods provided,
        // but we can verify the token is still valid
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
