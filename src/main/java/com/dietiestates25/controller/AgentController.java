package com.dietiestates25.controller;

import com.dietiestates25.dto.AgentCreateRequest;
import com.dietiestates25.dto.AgentDTO;
import com.dietiestates25.dto.AgentUpdateRequest;
import com.dietiestates25.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    @PreAuthorize("hasAuthority('AGENCY')")
    public ResponseEntity<Void> createAgent(@RequestBody AgentCreateRequest request) {
        agentService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getAgent(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.getAgent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAgent(@PathVariable Long id, @RequestBody AgentUpdateRequest request) {
        agentService.updateAgent(id, request);
        return ResponseEntity.ok().build();
    }
}
