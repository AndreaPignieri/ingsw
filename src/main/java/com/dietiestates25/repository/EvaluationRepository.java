package com.dietiestates25.repository;

import com.dietiestates25.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByAgentId(Long agentId);
}
