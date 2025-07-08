package com.example.audioservice.model.Response;

import lombok.Data;

@Data
public class ChallengeInfo {
    private Long id;
    private String fullSentence;
    private Integer orderIndex;
    private Long lessonId;
}
