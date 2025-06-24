package com.example.audioservice.controller;

import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import com.example.audioservice.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "section")
@RequestMapping("/api/sections")
public class SectionController {
    @Autowired
    private SectionService sectionService;
    @PostMapping("/add")
    public ResponseEntity<SectionResponse> addSection(@RequestBody SectionRequest request) {
        return sectionService.addSection(request);
    }
    @GetMapping("/list")
    public ResponseEntity<List<SectionResponse>> getAllSections(@RequestParam Long topicId) {
        return sectionService.getAllSections(topicId);
    }
}
