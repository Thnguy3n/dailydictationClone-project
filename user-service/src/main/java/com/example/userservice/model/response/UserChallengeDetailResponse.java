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
public class UserChallengeDetailResponse {
    private Long challengeId;
    private String fullSentence;
    private Integer orderIndex;
    private Integer isCompleted;
    private Integer attempts;
    private LocalDateTime completedAt;
    private Boolean currentlyPassed;
}
