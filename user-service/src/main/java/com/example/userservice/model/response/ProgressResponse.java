package com.example.userservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressResponse {
    private Long lessonId;
    private Long challengeId;
}
