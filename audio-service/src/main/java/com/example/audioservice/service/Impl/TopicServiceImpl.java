package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.CategoryEntity;
import com.example.audioservice.entity.TopicEntity;
import com.example.audioservice.model.Request.TopicRequest;
import com.example.audioservice.model.Response.TopicResponse;
import com.example.audioservice.repository.CategoryRepository;
import com.example.audioservice.repository.TopicRepository;
import com.example.audioservice.service.TopicService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Stream.builder;


@Service
public class TopicServiceImpl implements TopicService {
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        List<TopicEntity> topicEntities = topicRepository.findAll();
        List<TopicResponse> topicResponses = topicEntities.stream().map(t->modelMapper.map(t, TopicResponse.class)).collect(Collectors.toList());
        return new ResponseEntity<>(topicResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TopicResponse> addTopic(TopicRequest topicRequest) {
        CategoryEntity categoryEntity = categoryRepository.findById(topicRequest.getCategoryId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Category not found"));
            TopicEntity topicEntity = TopicEntity.builder()
                    .title(topicRequest.getTitle())
                    .levels(topicRequest.getLevels())
                    .description(topicRequest.getDescription())
                    .categoryEntity(categoryEntity)
                    .image(topicRequest.getImage()).build();
            topicRepository.save(topicEntity);
            return new ResponseEntity<>(modelMapper.map(topicEntity, TopicResponse.class), HttpStatus.CREATED);
    }
}
