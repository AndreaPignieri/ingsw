package com.dietiestates25.service;

import com.dietiestates25.dto.AgentCreateRequest;
import com.dietiestates25.dto.AgentDTO;
import com.dietiestates25.dto.AgentUpdateRequest;
import com.dietiestates25.model.Agent;
import com.dietiestates25.repository.AgentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private com.dietiestates25.repository.PropertyRepository propertyRepository;

    @InjectMocks
    private AgentService agentService;

    @Mock
    private com.dietiestates25.repository.UserRepository userRepository;

    @Mock
    private org.springframework.security.core.Authentication authentication;

    @Mock
    private org.springframework.security.core.context.SecurityContext securityContext;

    @Mock
    private org.springframework.security.core.userdetails.UserDetails userDetails;

    @Test
    void createAgent_ValidRequest_SavesAgent() {
        AgentCreateRequest request = new AgentCreateRequest();
        request.setFirstName("Agent");
        request.setLastName("Smith");
        request.setEmail("agent.smith@matrix.com");
        request.setPassword("neo");

        // Mock Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("manager@agency.com");
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // Mock Manager and Agency
        com.dietiestates25.model.User manager = new com.dietiestates25.model.User();
        manager.setEmail("manager@agency.com");
        com.dietiestates25.model.Agency agency = new com.dietiestates25.model.Agency();
        agency.setId(10L);
        manager.setAgency(agency);

        when(userRepository.findByEmail("manager@agency.com")).thenReturn(Optional.of(manager));
        when(passwordEncoder.encode("neo")).thenReturn("encodedPassword");

        agentService.createAgent(request);

        verify(agentRepository).save(argThat(agent -> agent.getEmail().equals("agent.smith@matrix.com") &&
                agent.getRoles().contains(com.dietiestates25.model.Role.AGENT) &&
                agent.getAgency().getId().equals(10L)));

        // Cleanup
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getAgent_ExistingId_ReturnsDTO() {
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);
        agent.setFirstName("Bond");

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));
        when(propertyRepository.findByAgentId(id)).thenReturn(java.util.Collections.emptyList());

        AgentDTO result = agentService.getAgent(id);

        assertEquals("Bond", result.getFirstName());
    }

    @Test
    void getAgent_NonExistingId_ThrowsException() {
        Long id = 99L;
        when(agentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> agentService.getAgent(id));
    }

    @Test
    void updateAgent_ExistingId_UpdatesFields() {
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);
        agent.setBiography("Old Bio");

        AgentUpdateRequest request = new AgentUpdateRequest();
        request.setBiography("New Bio");

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));

        agentService.updateAgent(id, request);

        assertEquals("New Bio", agent.getBiography());
        verify(agentRepository).save(agent);
    }

    @Test
    void updateAgent_NonExistingId_ThrowsException() {
        Long id = 99L;
        AgentUpdateRequest request = new AgentUpdateRequest();
        when(agentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> agentService.updateAgent(id, request));
    }
}
