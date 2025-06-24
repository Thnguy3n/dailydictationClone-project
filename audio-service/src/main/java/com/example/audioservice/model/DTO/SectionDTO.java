package com.example.audioservice.model.DTO;

import com.example.audioservice.entity.TopicEntity;
import lombok.Data;

@Data
public class SectionDTO {
    private Long id;
    private String title;
    private String level;
    private TopicEntity topicEntity;

}
