package com.dietiestates25.controller;

import com.dietiestates25.dto.AgentCreateRequest;
import com.dietiestates25.dto.AgentDTO;
import com.dietiestates25.dto.AgentUpdateRequest;
import com.dietiestates25.service.AgentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

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
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(authorities = "AGENCY") // Required for verify
    public void createAgent_Authorized_ReturnsCreated() throws Exception {
        AgentCreateRequest request = new AgentCreateRequest();
        request.setFirstName("New");
        request.setLastName("Agent");
        request.setEmail("test.agent@example.com");
        request.setPassword("strongpassword");

        mockMvc.perform(post("/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "USER") // Wrong authority
    public void createAgent_Unauthorized_ReturnsForbidden() throws Exception {
        AgentCreateRequest request = new AgentCreateRequest();

        mockMvc.perform(post("/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void getAgent_ReturnsAgent() throws Exception {
        AgentDTO agent = new AgentDTO();
        agent.setId(1L);
        agent.setFirstName("Agent007");

        when(agentService.getAgent(1L)).thenReturn(agent);

        mockMvc.perform(get("/agents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Agent007"));
    }

    @Test
    @WithMockUser
    public void updateAgent_ReturnsOk() throws Exception {
        AgentUpdateRequest request = new AgentUpdateRequest();
        request.setBiography("New Bio");

        mockMvc.perform(put("/agents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
