package com.dietiestates25.controller;

import com.dietiestates25.dto.EvaluationDTO;
import com.dietiestates25.dto.EvaluationRequest;
import com.dietiestates25.service.EvaluationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
public class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluationService evaluationService;

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
    @WithMockUser
    public void createEvaluation_ReturnsCreated() throws Exception {
        EvaluationRequest request = new EvaluationRequest();
        request.setScore(5);
        request.setComment("Nice");

        mockMvc.perform(post("/agents/1/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    public void getEvaluations_ReturnsList() throws Exception {
        EvaluationDTO eval = new EvaluationDTO();
        eval.setScore(5);
        eval.setComment("Nice");

        when(evaluationService.getEvaluations(1L)).thenReturn(List.of(eval));

        mockMvc.perform(get("/agents/1/evaluations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }
}
