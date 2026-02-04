package com.dietiestates25.service;

import com.dietiestates25.dto.ReviewCreateRequest;
import com.dietiestates25.dto.ReviewDTO;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Review;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private com.dietiestates25.repository.UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_ValidAgent_SavesReview() {
        Long agentId = 1L;
        Long userId = 2L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setScore(5);
        request.setComment("Great job!");
        request.setAgentId(agentId);

        Agent agent = new Agent();
        agent.setId(agentId);

        com.dietiestates25.model.User user = new com.dietiestates25.model.User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setScore(5);
        savedReview.setComment("Great job!");
        savedReview.setAgent(agent);
        savedReview.setUser(user);
        savedReview.setCreatedAt(java.time.LocalDateTime.now());

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewDTO result = reviewService.createReview(userId, request);

        assertNotNull(result);
        assertEquals(5, result.getScore());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_InvalidAgent_ThrowsException() {
        Long agentId = 99L;
        Long userId = 2L;
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAgentId(agentId);

        com.dietiestates25.model.User user = new com.dietiestates25.model.User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.createReview(userId, request));
    }

    @Test
    void getReviews_ValidAgent_ReturnsList() {
        Long agentId = 1L;
        // The service does not check existsById on getReviews anymore, it just returns
        // empty list if none found
        // or effectively filters by agentId

        Review review = new Review();
        review.setId(10L);
        review.setScore(4);
        review.setComment("Good");
        review.setCreatedAt(java.time.LocalDateTime.now());

        when(reviewRepository.findByAgentIdOrderByCreatedAtDesc(agentId)).thenReturn(List.of(review));

        List<ReviewDTO> result = reviewService.getReviewsByAgent(agentId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getScore());
    }
}
