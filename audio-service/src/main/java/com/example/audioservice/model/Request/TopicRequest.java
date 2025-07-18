package com.example.audioservice.model.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String levels;
    @NotBlank
    private String description;
    @NotBlank
    private String image;
    private Integer premiumTopic;
    private Long categoryId;
}
