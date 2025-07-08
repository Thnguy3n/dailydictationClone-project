package com.example.audioservice.model.Request;

import lombok.Data;

import java.util.List;

@Data
public class CheckRequest {
    private Long lessonId;
    private Integer orderIndex;
    private List<String> userAnswers;
}
