package com.example.audioservice.controller;

import com.example.audioservice.entity.TranscriptJob;
import com.example.audioservice.model.Request.TranscriptRequest;
import com.example.audioservice.model.Response.TranscriptJobResponse;
import com.example.audioservice.repository.TranscriptJobRepository;
import com.example.audioservice.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "transcript")
@RequestMapping("/api/transcript")
@RequiredArgsConstructor
public class TranscriptController {
    private final TranscriptService transcriptService;
    @PostMapping("/async")
    public ResponseEntity<TranscriptJobResponse> transcript(@RequestBody TranscriptRequest transcriptRequest) throws Exception {
        List<String> results =transcriptService.submitTranscriptJob(transcriptRequest);
        return ResponseEntity.ok(new TranscriptJobResponse(results.get(0), "SUBMITTED", results.get(1)));
    }
    @GetMapping("/status/{jobId}")
    public ResponseEntity<TranscriptJobResponse> getTranscriptStatus(@PathVariable String jobId) {
        TranscriptJobResponse response = transcriptService.getJobStatus(jobId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/result/{jobId}")
    public ResponseEntity<String> getTranscriptResult(@PathVariable String jobId) {
        String result = transcriptService.getJobResult(jobId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
