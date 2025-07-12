package com.example.audioservice.service;

import com.example.audioservice.model.Request.CheckRequest;
import com.example.audioservice.model.Response.AudioSegmentResponse;
import com.example.audioservice.model.Response.ChallengeInfo;
import com.example.audioservice.model.Response.ChallengeJobResponse;
import com.example.audioservice.model.Response.ChallengeResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ChallengeService {
    ResponseEntity<String> addChallenge(String answerKey, Long lessonId);
    ResponseEntity<List<ChallengeResponse>> findAllChallengesByLessonId(Long lessonId);
    ResponseEntity<List<ChallengeInfo>> findChallengesByLessonId(Long lessonId);
    ResponseEntity<Map<String, Object>> checkAnswer(CheckRequest checkRequest);
    ResponseEntity<List<AudioSegmentResponse>> segmentAudioForChallenges(Long lessonId) throws Exception;
    ResponseEntity<List<ChallengeJobResponse>> processChallenge(String challengeJobId, Long lessonId);

    ResponseEntity<Map<String, Object>> checkUserAnswer(CheckRequest checkRequest, String usernameFromToken);
    ResponseEntity<ChallengeResponse> getFirstChallenge(Long lessonId);
    ResponseEntity<ChallengeResponse> continueChallenges(Long lessonId, String username);

    ResponseEntity<ChallengeResponse> getNextChallenge(Long lessonId, Integer orderIndex);

    ResponseEntity<ChallengeResponse> getPreviousChallenge(Long lessonId, Integer orderIndex);
}
