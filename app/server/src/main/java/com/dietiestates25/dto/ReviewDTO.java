package com.dietiestates25.dto;

import com.dietiestates25.model.Review;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
    private String userFullName;

    public static ReviewDTO fromEntity(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setScore(review.getScore());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        if (review.getUser() != null) {
            dto.setUserFullName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        }
        return dto;
    }
}
