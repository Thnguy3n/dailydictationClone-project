package com.example.audioservice.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranscriptJobResponse {
    private String jobId;
    private String status;
    private String result;
    private String error;
    private String challengeJobId;
    public TranscriptJobResponse(String jobId, String status,String challengeJobId) {
        this.jobId = jobId;
        this.status = status;
        this.challengeJobId = challengeJobId;
    }
}
