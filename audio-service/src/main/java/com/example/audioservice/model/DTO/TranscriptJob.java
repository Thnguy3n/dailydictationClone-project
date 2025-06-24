package com.example.audioservice.model.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscriptJob {
    private String jobId;
    private String audioUrl;
}
