package com.example.audioservice.service;

import com.example.audioservice.model.Request.SectionFilter;
import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SectionService {
    ResponseEntity<SectionResponse> addSection(SectionRequest sectionRequest);
    ResponseEntity<List<SectionResponse>> getAllSections(Long topicId);
    ResponseEntity<List<SectionResponse>> getAllPremiumSections(Long topicId, String username);

    ResponseEntity<List<SectionResponse>> getSectionsByFilter(SectionFilter filter, Long topicId);

    ResponseEntity<List<SectionResponse>> getSectionsByFilterWithAuthenticated(SectionFilter filter, Long topicId, String username);
}
