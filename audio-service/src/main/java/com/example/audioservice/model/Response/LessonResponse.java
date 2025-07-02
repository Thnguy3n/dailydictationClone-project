package com.example.audioservice.model.Response;

import lombok.Data;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private Integer countChallenge;
}
