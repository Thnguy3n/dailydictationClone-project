package com.example.audioservice.controller;

import com.example.audioservice.model.Request.LessonRequest;
import com.example.audioservice.model.Response.LessonResponse;
import com.example.audioservice.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "lessons")
@RequestMapping("/api/lessons")
public class LessonController {
    private final LessonService lessonService;
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }
    @GetMapping("/list")
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        return lessonService.getAllLessons();
    }
    @PostMapping("/add")
    public ResponseEntity<LessonResponse> addLesson(@RequestBody LessonRequest lessonRequest) {
        return lessonService.addLesson(lessonRequest);
    }
}
