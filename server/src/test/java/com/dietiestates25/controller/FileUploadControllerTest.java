package com.dietiestates25.controller;

import com.dietiestates25.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileUploadController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    // Security Mocks
    @MockBean
    private com.dietiestates25.service.AuthService authService;
    @MockBean
    private com.dietiestates25.config.JwtAuthenticationFilter jwtAuthFilter;
    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    @MockBean
    private com.dietiestates25.security.CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    @MockBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

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
    @WithMockUser
    void uploadFile_ValidFile_ReturnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(storageService.store(any())).thenReturn("http://example.com/test.jpg");

        mockMvc.perform(multipart("/uploads")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://example.com/test.jpg"));
    }

    @Test
    @WithMockUser
    void uploadFile_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(storageService.store(any())).thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/uploads")
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Upload failed"));
    }
}
