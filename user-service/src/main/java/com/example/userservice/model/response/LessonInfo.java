package com.example.userservice.model.response;

import lombok.Data;

import java.util.List;

@Data
public class LessonInfo {
    private Long id;
    private String title;
    private String description;
    private List<Long> challengeIds;
}
