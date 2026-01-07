package com.dietiestates25.controller;

import com.dietiestates25.dto.LoginRequest;
import com.dietiestates25.dto.RegisterRequest;
import com.dietiestates25.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(AuthController.class)
@org.springframework.context.annotation.Import({
        com.dietiestates25.config.SecurityConfig.class,
        com.dietiestates25.exception.GlobalExceptionHandler.class
})
@SuppressWarnings("null")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Mock beans required by SecurityConfig
    @MockBean
    private com.dietiestates25.config.JwtAuthenticationFilter jwtAuthFilter;

    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    public void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldReturnBadRequestOnInvalidRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email"); // Invalid email format
        request.setPassword(""); // Empty password
        // Names missing is also valid for Bad Request testing, leaving as is or minimal
        // change

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnConflictIfEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFirstName("Existing");
        request.setLastName("User");

        doThrow(new com.dietiestates25.exception.UserAlreadyExistsException("Email already registered"))
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Note: We don't need to mock return value strictly for status check unless we
        // check body,
        // but default mock returns null which might cause 200 OK with empty body or
        // error depending on controller impl.
        // Controller returns result of authService.login which returns AuthResponse.
        // Mocking it to be safe.
        // when(authService.login(any(LoginRequest.class))).thenReturn(new
        // AuthResponse("token", new UserDTO()));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNotFoundIfUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password");

        doThrow(new com.dietiestates25.exception.ResourceNotFoundException("User not found")).when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
