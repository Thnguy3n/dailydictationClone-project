package com.example.audioservice.controller;

import com.example.audioservice.model.Request.TopicRequest;
import com.example.audioservice.model.Response.TopicResponse;
import com.example.audioservice.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:8080")
@RestController(value = "topic")
@RequestMapping("/api/topics")
public class TopicController {
    @Autowired
    private TopicService topicService;
    @GetMapping(value = "/list")
    private ResponseEntity<List<TopicResponse>> getAllTopics() {
        return topicService.getAllTopics();
    }
    @PostMapping(value = "/add")
    public ResponseEntity<TopicResponse> addTopic(@RequestBody TopicRequest topicRequest) {
        return topicService.addTopic(topicRequest);
    }

}
