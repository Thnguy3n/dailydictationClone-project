package com.example.audioservice.service;

import com.example.audioservice.model.Request.TopicRequest;
import com.example.audioservice.model.Response.TopicResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TopicService {
    ResponseEntity<List<TopicResponse>> getAllTopics();
    ResponseEntity<TopicResponse> addTopic(TopicRequest topicRequest);
}
