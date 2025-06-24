package com.example.audioservice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptMessage {
    private String jobId;
    private String audioUrl;
    private String challengeJobId;
}
