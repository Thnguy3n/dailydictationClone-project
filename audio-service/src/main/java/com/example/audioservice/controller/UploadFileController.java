package com.example.audioservice.controller;

import com.example.audioservice.service.LessonService;
import com.example.audioservice.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController(value = "upload")
@RequestMapping("/api/upload")
public class UploadFileController {
    private final LessonService lessonService;
    private final TopicService topicService;

    public UploadFileController(LessonService lessonService, TopicService topicService) {
        this.lessonService = lessonService;
        this.topicService = topicService;
    }
    @PostMapping("/audio")
    public ResponseEntity<String> uploadAudioFile(@RequestParam MultipartFile file) {
        try {
            return lessonService.audioPath(file);
        }
        catch (Exception e) {
            return  ResponseEntity.internalServerError().body("File upload failed");
        }
    }
    @PostMapping("/image")
    public ResponseEntity<String> uploadImageFile(@RequestParam MultipartFile file) {
        return topicService.uploadImage(file);
    }
}
