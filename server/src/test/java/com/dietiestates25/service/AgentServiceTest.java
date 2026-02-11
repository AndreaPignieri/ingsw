package com.dietiestates25.service;

import com.dietiestates25.dto.AgentCreateRequest;
import com.dietiestates25.dto.AgentDTO;
import com.dietiestates25.dto.AgentUpdateRequest;
import com.dietiestates25.exception.ResourceNotFoundException;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Agency;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.PropertyRepository;
import com.dietiestates25.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AgentService agentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Clear context before each test to avoid pollution
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAgent_ValidRequest_SavesAgent() {
        // Setup Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("manager@test.com");
        SecurityContextHolder.setContext(securityContext);

        // Setup Manager and Agency
        User manager = new User();
        Agency agency = new Agency();
        agency.setId(1L);
        manager.setAgency(agency);
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        // Setup Request
        AgentCreateRequest request = new AgentCreateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@doe.com");
        request.setPassword("password");
        request.setBiography("Bio");
        request.setProfilePhoto("photo.jpg");

        when(passwordEncoder.encode("password")).thenReturn("encoded");

        agentService.createAgent(request);

        verify(agentRepository).save(argThat(agent -> agent.getEmail().equals("john@doe.com") &&
                agent.getFirstName().equals("John") &&
                agent.getPasswordHash().equals("encoded") &&
                agent.getAgency().getId().equals(1L) &&
                agent.getRoles().contains(com.dietiestates25.model.Role.AGENT)));
    }

    @Test
    void createAgent_ManagerNotFound_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("manager@test.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.empty());

        AgentCreateRequest request = new AgentCreateRequest();
        assertThrows(ResourceNotFoundException.class, () -> agentService.createAgent(request));
    }

    @Test
    void createAgent_ManagerHasNoAgency_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("manager@test.com");
        SecurityContextHolder.setContext(securityContext);

        User manager = new User();
        manager.setAgency(null); // No agency
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        AgentCreateRequest request = new AgentCreateRequest();
        assertThrows(ResourceNotFoundException.class, () -> agentService.createAgent(request));
    }

    @Test
    void updateAgent_AllFields_UpdatesCorrectly() {
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));

        AgentUpdateRequest request = new AgentUpdateRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");
        request.setBiography("NewBio");
        request.setProfilePhoto("new.jpg");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setPhoneNumber("1234567890");

        agentService.updateAgent(id, request);

        assertEquals("NewFirst", agent.getFirstName());
        assertEquals("NewLast", agent.getLastName());
        assertEquals("NewBio", agent.getBiography());
        assertEquals("new.jpg", agent.getProfilePhoto());
        assertEquals(LocalDate.of(1990, 1, 1), agent.getBirthDate());
        assertEquals("1234567890", agent.getPhoneNumber());
        verify(agentRepository).save(agent);
    }

    @Test
    void updateAgent_PartialFields_UpdatesOnlyNotNull() {
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);
        agent.setFirstName("OldFirst");
        agent.setLastName("OldLast");

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));

        AgentUpdateRequest request = new AgentUpdateRequest();
        request.setFirstName("NewFirst");
        // match other fields as null

        agentService.updateAgent(id, request);

        assertEquals("NewFirst", agent.getFirstName());
        assertEquals("OldLast", agent.getLastName()); // unchanged
        verify(agentRepository).save(agent);
    }

    @Test
    void getAgent_WithProperties_MapsCorrectly() {
        Long id = 1L;
        Agent agent = new Agent();
        agent.setId(id);
        Agency agency = new Agency();
        agency.setName("Test Agency");
        agent.setAgency(agency);

        com.dietiestates25.model.Property property = new com.dietiestates25.model.Property();
        property.setId(100L);
        property.setTitle("Test Prop");
        property.setType(com.dietiestates25.model.PropertyType.SALE);

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));
        when(propertyRepository.findByAgentId(id)).thenReturn(Collections.singletonList(property));

        AgentDTO dto = agentService.getAgent(id);

        assertEquals(id, dto.getId());
        assertEquals("Test Agency", dto.getAgencyName());
        assertEquals(1, dto.getProperties().size());
        assertEquals("Test Prop", dto.getProperties().get(0).getTitle());
    }

    @Test
    void getAgentsByAgency_ReturnsList() {
        Long agencyId = 1L;
        Agent agent = new Agent();
        agent.setId(2L);

        when(agentRepository.findByAgencyId(agencyId)).thenReturn(Collections.singletonList(agent));
        when(propertyRepository.findByAgentId(2L)).thenReturn(Collections.emptyList());

        var result = agentService.getAgentsByAgency(agencyId);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void getAgentsByManagerEmail_Valid_ReturnsList() {
        String email = "manager@test.com";
        User manager = new User();
        Agency agency = new Agency();
        agency.setId(10L);
        manager.setAgency(agency);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(manager));

        Agent agent = new Agent();
        agent.setId(20L);
        when(agentRepository.findByAgencyId(10L)).thenReturn(Collections.singletonList(agent));
        when(propertyRepository.findByAgentId(20L)).thenReturn(Collections.emptyList());

        var result = agentService.getAgentsByManagerEmail(email);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
    }

    @Test
    void getAgentsByManagerEmail_ManagerHasNoAgency_ThrowsException() {
        String email = "manager@test.com";
        User manager = new User();
        manager.setAgency(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(manager));

        assertThrows(ResourceNotFoundException.class, () -> agentService.getAgentsByManagerEmail(email));
    }
}
