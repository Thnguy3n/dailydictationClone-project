package com.example.audioservice.model.Response;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicResponse {
    private Long id;
    private String title;
    private String levels;
    private String description;
    private String image;
    private String categoryTitle;
    private Integer premiumTopic;
}
