package com.dietiestates25.controller;

import com.dietiestates25.dto.PropertyDTO;
import com.dietiestates25.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(PropertyController.class)
@org.springframework.context.annotation.Import(com.dietiestates25.config.SecurityConfig.class)
public class PropertyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PropertyService propertyService;

        @MockBean
        private com.dietiestates25.service.AuthService authService;

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
        @org.springframework.security.test.context.support.WithMockUser
        @SuppressWarnings("null")
        public void shouldSearchProperties() throws Exception {
                PropertyDTO mockProperty = new PropertyDTO();
                mockProperty.setId(1L);
                mockProperty.setTitle("Test Villa");
                mockProperty.setCity("Napoli");
                mockProperty.setPrice(BigDecimal.valueOf(150000));

                mockProperty.setAmenities(new java.util.ArrayList<>());
                mockProperty.setPhotos(new java.util.ArrayList<>());

                java.util.List<PropertyDTO> properties = new java.util.ArrayList<>();
                properties.add(mockProperty);
                Page<PropertyDTO> mockPage = new PageImpl<>(properties,
                                org.springframework.data.domain.PageRequest.of(0, 10),
                                properties.size());

                when(propertyService.searchProperties(any(), any(), any(), any(), anyInt(), anyInt()))
                                .thenReturn(mockPage);

                mockMvc.perform(get("/properties")
                                .param("city", "Napoli")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value("Test Villa"))
                                .andExpect(jsonPath("$.content[0].city").value("Napoli"));
        }
}
