package com.example.audioservice.controller;

import com.example.audioservice.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController(value = "upload")
@RequestMapping("/api")
public class UploadFileController {
    private final LessonService lessonService;
    public UploadFileController(LessonService lessonService) {
        this.lessonService = lessonService;
    }
    @PostMapping("/audio/upload")
    public ResponseEntity<String> uploadAudioFile(@RequestParam MultipartFile file) {
        try {
            return lessonService.audioPath(file);
        }
        catch (Exception e) {
            return  ResponseEntity.internalServerError().body("File upload failed");
        }
    }
}
