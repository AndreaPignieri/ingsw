package com.dietiestates25.controller;

import com.dietiestates25.dto.ReviewCreateRequest;
import com.dietiestates25.dto.ReviewDTO;
import com.dietiestates25.service.ReviewService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(com.dietiestates25.config.SecurityConfig.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserService userService;

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
    @WithMockUser(username = "testuser")
    public void createReview_ReturnsCreated() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setScore(5);
        request.setComment("Nice");
        request.setAgentId(1L);

        com.dietiestates25.dto.UserDTO userDto = new com.dietiestates25.dto.UserDTO();
        userDto.setId(1L);
        when(userService.getUser("testuser")).thenReturn(userDto);

        mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getReviews_ReturnsList() throws Exception {
        ReviewDTO review = new ReviewDTO();
        review.setScore(5);
        review.setComment("Nice");

        when(reviewService.getReviewsByAgent(1L)).thenReturn(List.of(review));

        mockMvc.perform(get("/reviews/agent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5));
    }
}
