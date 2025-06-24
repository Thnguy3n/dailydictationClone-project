package com.example.audioservice.service;

import com.example.audioservice.model.Request.TranscriptRequest;
import com.example.audioservice.model.Response.TranscriptJobResponse;

import java.util.List;

public interface TranscriptService {

    List<String> submitTranscriptJob(TranscriptRequest transcriptRequest);
    TranscriptJobResponse getJobStatus(String jobId);

    String getJobResult(String jobId);
}
