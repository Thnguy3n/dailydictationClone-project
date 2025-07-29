package com.example.audioservice.model.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionFilter {
    private String level;
    private String lessonTitle;
    private String challenge_progress;
}
