package com.example.audioservice.controller;

import com.example.audioservice.model.Request.LessonRequest;
import com.example.audioservice.model.Response.LessonInfo;
import com.example.audioservice.model.Response.LessonResponse;
import com.example.audioservice.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "lessonsController")
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    @GetMapping("/list")
    public ResponseEntity<List<LessonResponse>> getAllLessons(@RequestParam Long sectionId) {
        return lessonService.getLessons(sectionId);
    }
    @PostMapping("/add")
    public ResponseEntity<LessonResponse> addLesson(@RequestBody LessonRequest lessonRequest) {
        return lessonService.addLesson(lessonRequest);
    }
    @GetMapping("/all-challenge")
    public ResponseEntity<List<LessonInfo>> getAllLessonsWithChallenge() {
        return lessonService.getAllLessonsWithChallenge();
    }
    @GetMapping("/{lessonId}/info")
    public ResponseEntity<LessonInfo> getLessonInfo(@PathVariable Long lessonId) {
        return lessonService.getLessonInfo(lessonId);
    }

}
