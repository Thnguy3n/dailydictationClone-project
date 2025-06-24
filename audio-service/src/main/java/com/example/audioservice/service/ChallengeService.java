package com.example.audioservice.service;

import com.example.audioservice.model.Request.ChallengeRequest;
import com.example.audioservice.model.Response.ChallengeJobResponse;
import com.example.audioservice.model.Response.ChallengeResponse;
import okhttp3.Challenge;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ChallengeService {
    ResponseEntity<String> addChallenge(String answerKey, Long lessonId);
    ResponseEntity<List<ChallengeResponse>> findAllChallengesByLessonId(Long lessonId);
    ResponseEntity<Map<String, Object>> checkUserAnswer(Long challengeId, List<String> userAnswers);

    ResponseEntity<Map<String, Object>> getChallengeData(Long challengeId);

    ResponseEntity<List<ChallengeJobResponse>> processChallenge(String challengeJobId, Long lessonId);
}
