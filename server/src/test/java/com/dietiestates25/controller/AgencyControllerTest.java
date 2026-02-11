package com.dietiestates25.controller;

import com.dietiestates25.dto.AgencyCreateRequest;
import com.dietiestates25.dto.AgencyDTO;
import com.dietiestates25.dto.AgencyUpdateRequest;
import com.dietiestates25.service.AgencyService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgencyController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
class AgencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgencyService agencyService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(authorities = "ADMIN")
    void createAgency_ValidRequest_ReturnsCreated() throws Exception {
        AgencyCreateRequest request = new AgencyCreateRequest();
        request.setName("Test Agency");
        request.setPhone("1234567890");
        request.setEmail("agency@test.com");
        request.setManagerEmail("manager@test.com");
        request.setManagerFirstName("Manager");
        request.setManagerLastName("Test");
        request.setManagerPassword("password");

        mockMvc.perform(post("/agencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(agencyService).createAgency(any(AgencyCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "agency@test.com", authorities = "AGENCY")
    void getMyAgency_ReturnsAgency() throws Exception {
        AgencyDTO mockAgency = new AgencyDTO();
        mockAgency.setId(1L);
        mockAgency.setName("Test Agency");
        mockAgency.setEmail("agency@test.com");

        when(agencyService.getAgencyByUser("agency@test.com")).thenReturn(mockAgency);

        mockMvc.perform(get("/agencies/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("agency@test.com"));
    }

    @Test
    @WithMockUser(username = "agency@test.com", authorities = "AGENCY")
    void updateMyAgency_ValidRequest_ReturnsOk() throws Exception {
        AgencyUpdateRequest request = new AgencyUpdateRequest();
        request.setName("Updated Agency");

        mockMvc.perform(put("/agencies/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(agencyService).updateAgency(eq("agency@test.com"), any(AgencyUpdateRequest.class));
    }
}
