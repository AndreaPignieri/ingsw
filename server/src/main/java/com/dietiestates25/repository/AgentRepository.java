package com.dietiestates25.repository;

import com.dietiestates25.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    java.util.Optional<Agent> findByEmail(String email);

    java.util.List<Agent> findByAgencyId(Long agencyId);
}
