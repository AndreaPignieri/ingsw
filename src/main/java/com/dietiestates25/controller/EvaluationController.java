package com.dietiestates25.controller;

import com.dietiestates25.dto.EvaluationDTO;
import com.dietiestates25.dto.EvaluationRequest;
import com.dietiestates25.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agents/{agentId}/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<Void> createEvaluation(@PathVariable Long agentId, @RequestBody EvaluationRequest request) {
        evaluationService.createEvaluation(agentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<EvaluationDTO>> getEvaluations(@PathVariable Long agentId) {
        return ResponseEntity.ok(evaluationService.getEvaluations(agentId));
    }
}
