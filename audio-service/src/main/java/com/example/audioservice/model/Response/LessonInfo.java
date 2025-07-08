package com.example.audioservice.model.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class LessonInfo {
    private Long id;
    private String title;
    private List<Long> challengeIds;
}
