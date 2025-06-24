package com.example.audioservice.model.DTO;

import com.example.audioservice.model.Response.AssemblyWordInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SentenceWithTiming {
    private String text;
    private Double startTime;
    private Double endTime;
    private List<AssemblyWordInfoResponse> words;
}