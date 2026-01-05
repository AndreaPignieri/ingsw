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

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public void createAgent(AgentCreateRequest request) {
        Agent agent = new Agent();
        agent.setFirstName(request.getFirstName());
        agent.setLastName(request.getLastName());
        agent.setEmail(request.getEmail());
        agent.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        agent.setBiography(request.getBiography());
        agent.setProfilePhoto(request.getProfilePhoto());

        // Since Agent extends User, we must set User fields too if creating from
        // scratch
        // Handling user part implicitly by inheritance

        agent.getRoles().add(com.dietiestates25.model.Role.AGENT);

        agentRepository.save(agent);
    }

    @SuppressWarnings("null")
    public AgentDTO getAgent(Long id) {
        Agent agent = agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));
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
        agentRepository.save(agent);
    }

    private AgentDTO mapToDTO(Agent agent) {
        AgentDTO dto = new AgentDTO();
        dto.setId(agent.getId());
        dto.setFirstName(agent.getFirstName());
        dto.setLastName(agent.getLastName());
        dto.setBiography(agent.getBiography());
        dto.setProfilePhoto(agent.getProfilePhoto());
        return dto;
    }
}
