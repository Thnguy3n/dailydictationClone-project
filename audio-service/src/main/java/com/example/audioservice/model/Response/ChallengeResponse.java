package com.example.audioservice.model.Response;

import lombok.Data;

@Data
public class ChallengeResponse {
    private Integer orderIndex;
    private String fullSentence;
    private String wordData;
    private Double startTime;
    private Double endTime;
    private String audioSegmentUrl;
    private Integer isPass;
}
