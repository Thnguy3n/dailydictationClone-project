package com.example.userservice.service;

import com.example.userservice.model.request.SectionProgressFilterRequest;
import com.example.userservice.model.response.ProgressResponse;
import com.example.userservice.model.response.UserChallengeDetailResponse;
import com.example.userservice.model.response.UserLessonProgressDetailResponse;
import com.example.userservice.model.response.UserLessonProgressResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserChallengeProgressService {
    ResponseEntity<List<UserLessonProgressResponse>> getUserLessonProgress(String username);
    ResponseEntity<UserLessonProgressDetailResponse> getUserLessonProgressDetail(String username, Long lessonId);

    ResponseEntity<ProgressResponse> getLatestCompleteChallengeDetail(String username, Long lessonId);

    ResponseEntity<List<Long>> getSectionIdsByProgressFilter(SectionProgressFilterRequest request);
}
