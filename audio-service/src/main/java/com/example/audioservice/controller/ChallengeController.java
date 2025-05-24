package com.example.audioservice.controller;

import com.example.audioservice.model.Request.ChallengeRequest;
import com.example.audioservice.model.Response.ChallengeResponse;
import com.example.audioservice.service.ChallengeService;
import okhttp3.Challenge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/list")
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges(@RequestParam Long lessonId) {
        return challengeService.findAllChallengesByLessonId(lessonId);
    }
}
