package com.example.audioservice.model.DTO;

import com.example.audioservice.entity.SectionEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String audio;
    private String transcript;
    private String answerKey;
    private SectionEntity sectionEntity;

}
