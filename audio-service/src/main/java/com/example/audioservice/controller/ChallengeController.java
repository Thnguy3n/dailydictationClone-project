package com.example.audioservice.controller;

import com.example.audioservice.model.Request.ChallengeRequest;
import com.example.audioservice.model.Response.ChallengeJobResponse;
import com.example.audioservice.model.Response.ChallengeResponse;
import com.example.audioservice.service.ChallengeService;
import okhttp3.Challenge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController(value = "addChallengeOfAdmin")
@RequestMapping("/api/challenge")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addChallenge(@RequestBody String answerKey,
                                               @RequestParam Long lessonId) {
        return challengeService.addChallenge(answerKey,lessonId);
    }
    @GetMapping("/process/{challengeJobId}")
    public ResponseEntity<List<ChallengeJobResponse>> getChallengeById(@PathVariable String challengeJobId, @RequestParam Long lessonId) {
        return challengeService.processChallenge(challengeJobId, lessonId);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges(@RequestParam Long lessonId) {
        return challengeService.findAllChallengesByLessonId(lessonId);
    }
    @PostMapping("/check/{challengeId}")
    public ResponseEntity<Map<String, Object>> checkAnswer(
            @PathVariable Long challengeId,
            @RequestBody List<String> userAnswers) {
        return challengeService.checkUserAnswer(challengeId, userAnswers);
    }
}
