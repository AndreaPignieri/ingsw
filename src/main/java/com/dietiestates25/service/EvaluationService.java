package com.dietiestates25.service;

import com.dietiestates25.dto.EvaluationDTO;
import com.dietiestates25.dto.EvaluationRequest;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Evaluation;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final AgentRepository agentRepository;
    private final com.dietiestates25.repository.UserRepository userRepository;

    @Transactional
    @SuppressWarnings("null")
    public void createEvaluation(Long agentId, EvaluationRequest request) {
        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new RuntimeException("Agent not found"));

        // Get current user email from SecurityContext
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
        com.dietiestates25.model.User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Evaluation evaluation = new Evaluation();
        evaluation.setAgent(agent);
        evaluation.setScore(request.getScore());
        evaluation.setComment(request.getComment());
        evaluation.setUser(currentUser);

        evaluationRepository.save(evaluation);
    }

    @SuppressWarnings("null")
    public List<EvaluationDTO> getEvaluations(Long agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new RuntimeException("Agent not found");
        }
        return evaluationRepository.findByAgentId(agentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EvaluationDTO mapToDTO(Evaluation e) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(e.getId());
        dto.setScore(e.getScore());
        dto.setComment(e.getComment());
        dto.setDate(e.getCreatedAt());
        return dto;
    }
}
