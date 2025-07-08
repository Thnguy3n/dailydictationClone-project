package com.example.audioservice.service;
import com.example.audioservice.model.Request.LessonRequest;
import com.example.audioservice.model.Response.LessonInfo;
import com.example.audioservice.model.Response.LessonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonService {
    ResponseEntity<List<LessonResponse>>getLessons(Long sectionId);
    ResponseEntity<LessonResponse>addLesson(LessonRequest lessonRequest);
    ResponseEntity<String> audioPath(MultipartFile file) throws IOException;
    ResponseEntity<List<LessonInfo>> getAllLessonsWithChallenge();
    ResponseEntity<LessonInfo> getLessonInfo(Long lessonId);
}
