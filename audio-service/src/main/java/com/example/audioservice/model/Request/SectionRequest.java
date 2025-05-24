package com.example.audioservice.model.Request;

import com.example.audioservice.entity.TopicEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SectionRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String level;
    private Long topicId;
}
