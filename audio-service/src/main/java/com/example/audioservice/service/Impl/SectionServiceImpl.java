package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.entity.TopicEntity;
import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import com.example.audioservice.repository.SectionRepository;
import com.example.audioservice.repository.TopicRepository;
import com.example.audioservice.service.SectionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;

    public SectionServiceImpl(SectionRepository sectionRepository, TopicRepository topicRepository, ModelMapper modelMapper) {
        this.sectionRepository = sectionRepository;
        this.topicRepository = topicRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ResponseEntity<SectionResponse> addSection(SectionRequest sectionRequest) {
        TopicEntity topicEntity = topicRepository.findById(sectionRequest.getTopicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic Not Found"));
        SectionEntity sectionEntity = SectionEntity.builder()
                .title(sectionRequest.getTitle())
                .level(sectionRequest.getLevel())
                .topicEntity(topicEntity).build();
        sectionRepository.save(sectionEntity);
        return new ResponseEntity<>(modelMapper.map(sectionEntity, SectionResponse.class), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<SectionResponse>> getAllSections(Long topicId) {
        List<SectionEntity> sections = sectionRepository.findAllByTopicEntity_Id(topicId);
        List<SectionResponse> sectionResponses = sections.stream()
                .map(sectionEntity -> modelMapper.map(sectionEntity, SectionResponse.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(sectionResponses, HttpStatus.OK);
    }
}
