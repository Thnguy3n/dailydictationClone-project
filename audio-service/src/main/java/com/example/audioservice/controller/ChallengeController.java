package com.example.audioservice.controller;

import com.example.audioservice.model.Request.CheckRequest;
import com.example.audioservice.model.Response.AudioSegmentResponse;
import com.example.audioservice.model.Response.ChallengeInfo;
import com.example.audioservice.model.Response.ChallengeJobResponse;
import com.example.audioservice.model.Response.ChallengeResponse;
import com.example.audioservice.service.AudioProcessingService;
import com.example.audioservice.service.ChallengeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController(value = "addChallengeOfAdmin")
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;
    private final AudioProcessingService audioProcessingService;
    @Value("${jwt-secret}")
    private String secretKey;

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
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAnswer(
            HttpServletRequest request,
            @RequestBody CheckRequest checkRequest) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return challengeService.checkAnswer(checkRequest);
        }
        String token = header.substring(7);
        return challengeService.checkUserAnswer(checkRequest, getUsernameFromToken(token));
    }
    @PostMapping("/segment-audio")
    public ResponseEntity<List<AudioSegmentResponse>> segmentAudioForChallenges(
            @RequestParam Long lessonId) {
        try {
            return challengeService.segmentAudioForChallenges(lessonId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/lesson/{lessonId}/info")
    public ResponseEntity<List<ChallengeInfo>> getChallengesByLessonId(@PathVariable Long lessonId) {
        return challengeService.findChallengesByLessonId(lessonId);
    }
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

}
