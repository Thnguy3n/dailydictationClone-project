package com.example.audioservice.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioSegmentResponse {
    private Long challengeId;
    private Integer orderIndex;
    private String fullSentence;
    private String audioUrl;
    private Double startTime;
    private Double endTime;
    private String status;
    private String error;
}
