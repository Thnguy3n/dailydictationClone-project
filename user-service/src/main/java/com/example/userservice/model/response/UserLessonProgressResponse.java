package com.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgressResponse {
    private Long lessonId;
    private String lessonTitle;
    private Integer totalChallenges;
    private Integer attemptedChallenges;
    private Integer passedChallenges;
    private Double progressPercentage;
    private Double passPercentage;
    private String status;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;
}