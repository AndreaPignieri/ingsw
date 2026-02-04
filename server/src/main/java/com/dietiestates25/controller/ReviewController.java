package com.dietiestates25.controller;

import com.dietiestates25.dto.ReviewCreateRequest;
import com.dietiestates25.dto.ReviewDTO;
import com.dietiestates25.dto.UserDTO;
import com.dietiestates25.service.ReviewService;
import com.dietiestates25.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ReviewDTO>> getAgentReviews(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewService.getReviewsByAgent(agentId));
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserDTO userDto = userService.getUser(email);

        return ResponseEntity.ok(reviewService.createReview(userDto.getId(), request));
    }
}
