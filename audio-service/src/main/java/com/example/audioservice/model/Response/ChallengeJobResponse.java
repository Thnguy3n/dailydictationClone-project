package com.example.audioservice.model.Response;

import lombok.Data;

@Data
public class ChallengeJobResponse {
    private String fullSentence;
    private Double startTime;
    private Double endTime;
}
