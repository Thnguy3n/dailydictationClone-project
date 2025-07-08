package com.example.userservice.model.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgressDetailResponse {
    private UserLessonProgressResponse lessonProgress;
    private List<UserChallengeDetailResponse> challengeDetails;
}