package com.example.audioservice.model.Response;

import com.example.audioservice.entity.TopicEntity;
import lombok.Data;

@Data
public class SectionResponse {
    private Long id;
    private String title;
    private String level;
}
