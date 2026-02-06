package com.dietiestates25.service;

import com.dietiestates25.dto.AgentCreateRequest;
import com.dietiestates25.dto.AgentDTO;
import com.dietiestates25.dto.AgentUpdateRequest;
import com.dietiestates25.model.Agent;
import com.dietiestates25.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final com.dietiestates25.repository.UserRepository userRepository;
    private final com.dietiestates25.repository.PropertyRepository propertyRepository;

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public void createAgent(AgentCreateRequest request) {
        // Get currently authenticated manager
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();

        // Find the manager in DB to get their Agency
        com.dietiestates25.model.User manager = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getAgency() == null) {
            throw new RuntimeException("Manager is not associated with any Agency");
        }

        Agent agent = new Agent();
        agent.setFirstName(request.getFirstName());
        agent.setLastName(request.getLastName());
        agent.setEmail(request.getEmail());
        agent.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        agent.setBiography(request.getBiography());
        agent.setProfilePhoto(request.getProfilePhoto());
        agent.setAgency(manager.getAgency());

        agent.getRoles().add(com.dietiestates25.model.Role.AGENT);

        agentRepository.save(agent);
    }

    public java.util.List<AgentDTO> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<AgentDTO> getAgentsByAgency(Long agencyId) {
        return agentRepository.findByAgencyId(agencyId).stream()
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<AgentDTO> getAgentsByManagerEmail(String email) {
        com.dietiestates25.model.User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getAgency() == null) {
            throw new RuntimeException("Manager is not associated with any Agency");
        }

        return getAgentsByAgency(manager.getAgency().getId());
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public AgentDTO getAgent(Long id) {
        System.out.println("DEBUG: Fetching agent with ID: " + id);
        Agent agent = null;
        try {
            agent = agentRepository.findById(id)
                    .orElseThrow(() -> new com.dietiestates25.exception.ResourceNotFoundException(
                            "Agent not found with id: " + id));
        } catch (com.dietiestates25.exception.ResourceNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR in getAgent -> findById: " + t.getMessage());
            t.printStackTrace();
            throw new RuntimeException("Crash during agent fetch", t);
        }

        System.out.println("DEBUG: Agent found: " + agent.getId());
        return mapToDTO(agent);
    }

    @Transactional
    @SuppressWarnings("null")
    public void updateAgent(Long id, AgentUpdateRequest request) {
        Agent agent = agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));
        if (request.getFirstName() != null)
            agent.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            agent.setLastName(request.getLastName());
        if (request.getBiography() != null)
            agent.setBiography(request.getBiography());
        if (request.getProfilePhoto() != null)
            agent.setProfilePhoto(request.getProfilePhoto());
        if (request.getBirthDate() != null)
            agent.setBirthDate(request.getBirthDate());
        if (request.getPhoneNumber() != null)
            agent.setPhoneNumber(request.getPhoneNumber());
        agentRepository.save(agent);
    }

    private AgentDTO mapToDTO(Agent agent) {
        try {
            System.out.println("DEBUG: Mapping Agent to DTO: " + agent.getId());
            AgentDTO dto = new AgentDTO();
            dto.setId(agent.getId());
            dto.setFirstName(agent.getFirstName());
            dto.setLastName(agent.getLastName());
            dto.setEmail(agent.getEmail());
            dto.setBiography(agent.getBiography());
            dto.setProfilePhoto(agent.getProfilePhoto());
            dto.setBirthDate(agent.getBirthDate());
            dto.setPhoneNumber(agent.getPhoneNumber());

            System.out.println("DEBUG: Basic agent fields mapped.");

            if (agent.getAgency() != null) {
                dto.setAgencyName(agent.getAgency().getName());
                System.out.println("DEBUG: Agency name set: " + agent.getAgency().getName());
            }

            // Fetch properties
            System.out.println("DEBUG: Fetching properties for agent " + agent.getId());
            var properties = propertyRepository.findByAgentId(agent.getId());
            System.out.println("DEBUG: Found " + (properties != null ? properties.size() : "null") + " properties.");

            if (properties != null) {
                dto.setProperties(
                        properties.stream().map(this::mapPropertyToDTO).collect(java.util.stream.Collectors.toList()));
            } else {
                dto.setProperties(new java.util.ArrayList<>());
            }

            System.out.println("DEBUG: Properties mapped.");

            return dto;
        } catch (Exception e) {
            System.err.println("ERROR mapping agent to DTO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private com.dietiestates25.dto.PropertyDTO mapPropertyToDTO(com.dietiestates25.model.Property property) {
        com.dietiestates25.dto.PropertyDTO dto = new com.dietiestates25.dto.PropertyDTO();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setPrice(property.getPrice());
        dto.setType(property.getType().name());
        dto.setCity(property.getCity());
        dto.setRooms(property.getRooms());
        dto.setFloor(property.getFloor());
        dto.setEnergyClass(property.getEnergyClass() != null ? property.getEnergyClass().name() : null);

        if (property.getLocation() != null) {
            dto.setLatitude(java.math.BigDecimal.valueOf(property.getLocation().getY()));
            dto.setLongitude(java.math.BigDecimal.valueOf(property.getLocation().getX()));
        }

        // Basic mapping, skip heavy relationships if needed, or map them as simplified
        // Currently PropertyDTO expects lists of strings for amenities and photos
        // Handle potential null collections
        if (property.getPhotos() != null) {
            dto.setPhotos(property.getPhotos().stream().map(com.dietiestates25.model.PropertyPhoto::getUrl)
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            dto.setPhotos(new java.util.ArrayList<>());
        }

        if (property.getAmenities() != null) {
            dto.setAmenities(property.getAmenities().stream().map(com.dietiestates25.model.Amenity::getName)
                    .collect(java.util.stream.Collectors.toList()));
        } else {
            dto.setAmenities(new java.util.ArrayList<>());
        }

        return dto;
    }
}
