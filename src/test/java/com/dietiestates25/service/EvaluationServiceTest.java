package com.dietiestates25.service;

import com.dietiestates25.dto.EvaluationDTO;
import com.dietiestates25.dto.EvaluationRequest;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Evaluation;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.EvaluationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private com.dietiestates25.repository.UserRepository userRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void createEvaluation_ValidAgent_SavesEvaluation() {
        Long agentId = 1L;
        EvaluationRequest request = new EvaluationRequest();
        request.setScore(5);
        request.setComment("Great job!");

        Agent agent = new Agent();
        agent.setId(agentId);

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        // Security Context Mock
        org.springframework.security.core.Authentication authentication = mock(
                org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(
                org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("reviewer@example.com");
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        com.dietiestates25.model.User reviewer = new com.dietiestates25.model.User();
        reviewer.setEmail("reviewer@example.com");
        when(userRepository.findByEmail("reviewer@example.com")).thenReturn(Optional.of(reviewer));

        evaluationService.createEvaluation(agentId, request);

        verify(evaluationRepository).save(any(Evaluation.class));

        // Clean up
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void createEvaluation_InvalidAgent_ThrowsException() {
        Long agentId = 99L;
        EvaluationRequest request = new EvaluationRequest();
        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> evaluationService.createEvaluation(agentId, request));
    }

    @Test
    void getEvaluations_ValidAgent_ReturnsList() {
        Long agentId = 1L;
        when(agentRepository.existsById(agentId)).thenReturn(true);

        Evaluation eval = new Evaluation();
        eval.setId(10L);
        eval.setScore(4);
        eval.setComment("Good");
        // Simulate @PrePersist behavior
        eval.setCreatedAt(java.time.LocalDateTime.now());
        // trigger/default but logic handles null?
        // Service mapToDTO: if dto.getDate() == null -> set now.

        when(evaluationRepository.findByAgentId(agentId)).thenReturn(List.of(eval));

        List<EvaluationDTO> result = evaluationService.getEvaluations(agentId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getScore());
    }

    @Test
    void getEvaluations_InvalidAgent_ThrowsException() {
        Long agentId = 99L;
        when(agentRepository.existsById(agentId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> evaluationService.getEvaluations(agentId));
    }
}
