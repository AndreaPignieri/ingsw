package com.dietiestates25.service;

import com.dietiestates25.dto.AgencyCreateRequest;
import com.dietiestates25.dto.AgencyDTO;
import com.dietiestates25.dto.AgencyUpdateRequest;
import com.dietiestates25.exception.ResourceNotFoundException;
import com.dietiestates25.exception.UserAlreadyExistsException;
import com.dietiestates25.model.Agency;

import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgencyRepository;
import com.dietiestates25.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AgencyService agencyService;

    private AgencyCreateRequest createRequest;
    private Agency agency;
    private User manager;

    @BeforeEach
    void setUp() {
        createRequest = new AgencyCreateRequest();
        createRequest.setName("Test Agency");
        createRequest.setAddress("Test Address");
        createRequest.setPhone("1234567890");
        createRequest.setEmail("agency@test.com");
        createRequest.setManagerEmail("manager@test.com");
        createRequest.setManagerFirstName("Manager");
        createRequest.setManagerLastName("Test");
        createRequest.setManagerPassword("password");

        agency = new Agency();
        agency.setId(1L);
        agency.setName("Test Agency");
        agency.setAddress("Test Address");
        agency.setPhone("1234567890");
        agency.setEmail("agency@test.com");

        manager = new User();
        manager.setEmail("manager@test.com");
        manager.setAgency(agency);
    }

    @Test
    void createAgency_ValidRequest_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        agencyService.createAgency(createRequest);

        verify(agencyRepository).save(any(Agency.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAgency_ManagerAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);

        assertThrows(UserAlreadyExistsException.class, () -> agencyService.createAgency(createRequest));
    }

    @Test
    void getAgencyByUser_UserExists_ReturnsAgency() {
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        AgencyDTO result = agencyService.getAgencyByUser("manager@test.com");

        assertNotNull(result);
        assertEquals(agency.getName(), result.getName());
    }

    @Test
    void getAgencyByUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agencyService.getAgencyByUser("unknown@test.com"));
    }

    @Test
    void getAgencyByUser_UserNoAgency_ThrowsException() {
        User userNoAgency = new User();
        userNoAgency.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userNoAgency));

        assertThrows(ResourceNotFoundException.class, () -> agencyService.getAgencyByUser("user@test.com"));
    }

    @Test
    void updateAgency_UserExists_Success() {
        AgencyUpdateRequest updateRequest = new AgencyUpdateRequest();
        updateRequest.setName("Updated Agency");

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));

        agencyService.updateAgency("manager@test.com", updateRequest);

        verify(agencyRepository).save(agency);
        assertEquals("Updated Agency", agency.getName());
    }
}
