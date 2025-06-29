package com.example.audioservice.service;

import com.example.audioservice.model.DTO.AudioSegment;
import com.example.audioservice.model.Response.AudioSegmentResponse;

import java.util.List;

public interface AudioProcessingService {
    List<AudioSegmentResponse> segmentAudio(String audioUrl, List<AudioSegment> segments) throws Exception;

    String downloadAudioFromFirebase(String firebaseUrl) throws Exception;

    String uploadSegmentToFirebase(String segmentFilePath, String fileName) throws Exception;
}
