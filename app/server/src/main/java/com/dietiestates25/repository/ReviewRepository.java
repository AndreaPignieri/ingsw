package com.dietiestates25.repository;

import com.dietiestates25.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByAgentIdOrderByCreatedAtDesc(Long agentId);
}
