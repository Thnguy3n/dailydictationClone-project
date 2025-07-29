package com.example.audioservice.controller;

import com.example.audioservice.model.Request.SectionFilter;
import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import com.example.audioservice.service.SectionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "section")
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;
    @Value("${jwt-secret}")
    private String secretKey;
    @PostMapping("/add")
    public ResponseEntity<SectionResponse> addSection(@RequestBody SectionRequest request) {
        return sectionService.addSection(request);
    }
    @PostMapping("/filter")
    public ResponseEntity<List<SectionResponse>> getSectionsByFilter(@RequestBody SectionFilter filter,
                                                                     @RequestParam Long topicId,
                                                                     HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.trim().isEmpty() || !header.startsWith("Bearer ")) {
            return sectionService.getSectionsByFilter(filter, topicId);
        }
        String token = header.substring(7);
        String username = getUsernameFromToken(token);
        return sectionService.getSectionsByFilterWithAuthenticated(filter, topicId, username);
    }
    @GetMapping("/list")
    public ResponseEntity<List<SectionResponse>> getAllSections(@RequestParam Long topicId,
                                                                HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.trim().isEmpty() || !header.startsWith("Bearer ")) {
            return sectionService.getAllSections(topicId);
        }
        String token = header.substring(7);
        String username = getUsernameFromToken(token);
        return sectionService.getAllPremiumSections(topicId, username);
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
