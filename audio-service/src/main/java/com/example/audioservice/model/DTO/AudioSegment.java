package com.example.audioservice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioSegment {
    private Long challengeId;
    private Long lessonId;
    private Integer orderIndex;
    private String fullSentence;
    private Double startTime; // milliseconds
    private Double endTime;   // milliseconds
    private String fileName;
}