package com.example.audioservice.controller;

import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.example.audioservice.model.Request.TranscriptRequest;
import com.example.audioservice.service.TranscriptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController(value = "transcript")
@RequestMapping("/api/transcript")
public class TranscriptController {
    private final TranscriptService transcriptService;
    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }
    @PostMapping
    public ResponseEntity<String> transcript(@RequestBody TranscriptRequest transcriptRequest) throws Exception {
        return new ResponseEntity<>(transcriptService.Transcript(transcriptRequest), HttpStatus.OK);
    }

}
