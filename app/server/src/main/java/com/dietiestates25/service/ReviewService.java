package com.dietiestates25.service;

import com.dietiestates25.dto.ReviewCreateRequest;
import com.dietiestates25.dto.ReviewDTO;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Review;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.ReviewRepository;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dietiestates25.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByAgent(Long agentId) {
        return reviewRepository.findByAgentIdOrderByCreatedAtDesc(agentId).stream()
                .map(ReviewDTO::fromEntity)
                .toList();
    }

    @Transactional
    public ReviewDTO createReview(Long userId, ReviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        Review review = new Review();
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setAgent(agent);
        review.setUser(user);

        // Ensure createdAt is set if PrePersist doesn't fire immediately or for safety
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        Review savedReview = reviewRepository.save(review);
        return ReviewDTO.fromEntity(savedReview); // Ensure we return the DTO
    }
}
