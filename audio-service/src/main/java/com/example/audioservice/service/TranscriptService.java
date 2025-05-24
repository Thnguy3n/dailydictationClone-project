package com.example.audioservice.service;

import com.example.audioservice.model.Request.TranscriptRequest;

public interface TranscriptService {
    String Transcript(TranscriptRequest transcriptRequest) throws Exception;
}
