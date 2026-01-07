package com.dietiestates25.controller;

import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.dto.UserUpdateRequest;
import com.dietiestates25.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Security Mocks
    @MockBean
    private com.dietiestates25.service.AuthService authService;
    @MockBean
    private com.dietiestates25.config.JwtAuthenticationFilter jwtAuthFilter;
    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Bypass JWT Filter
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "me@example.com")
    public void getMyProfile_ReturnsUser() throws Exception {
        UserDTO mockUser = new UserDTO();
        mockUser.setId(1L);
        mockUser.setEmail("me@example.com");

        when(userService.getUser("me@example.com")).thenReturn(mockUser);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    public void updateMyProfile_ValidRequest_ReturnsOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");

        mockMvc.perform(put("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
