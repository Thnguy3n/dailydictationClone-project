package com.example.audioservice.model.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String audioPath;
    @NotBlank
    private String transcript;
    @NotNull
    private Long sectionId;
}
