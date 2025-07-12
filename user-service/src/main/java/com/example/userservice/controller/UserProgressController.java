package com.example.userservice.controller;

import com.example.userservice.model.response.ProgressResponse;
import com.example.userservice.model.response.UserChallengeDetailResponse;
import com.example.userservice.model.response.UserLessonProgressDetailResponse;
import com.example.userservice.model.response.UserLessonProgressResponse;
import com.example.userservice.service.UserChallengeProgressService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-progress")
@RequiredArgsConstructor
public class UserProgressController {
    private final UserChallengeProgressService userProgressService;
    @Value("${jwt-secret}")
    private String secretKey;
    @GetMapping("/lessons")
    public ResponseEntity<List<UserLessonProgressResponse>> getUserLessonProgress(
            HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = header.substring(7);
        String username = getUsernameFromToken(token);
        return userProgressService.getUserLessonProgress(username);
    }
    @GetMapping("/lessons/{lessonId}/detail")
    public ResponseEntity<UserLessonProgressDetailResponse> getUserLessonProgressDetail(
            HttpServletRequest request,
            @PathVariable Long lessonId) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = header.substring(7);
        return userProgressService.getUserLessonProgressDetail(getUsernameFromToken(token), lessonId);
    }
    @GetMapping("/last-completed-challenge/{lessonId}")
    public ResponseEntity<ProgressResponse> getUserChallengeDetail(@RequestParam String username,
                                                                   @PathVariable Long lessonId) {
        return userProgressService.getLatestCompleteChallengeDetail(username,lessonId);
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
