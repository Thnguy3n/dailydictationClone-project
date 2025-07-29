package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.entity.TopicEntity;
import com.example.audioservice.model.Request.SectionFilter;
import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import com.example.audioservice.repository.SectionRepository;
import com.example.audioservice.repository.TopicRepository;
import com.example.audioservice.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity<SectionResponse> addSection(SectionRequest sectionRequest) {
        TopicEntity topicEntity = topicRepository.findById(sectionRequest.getTopicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic Not Found"));
        SectionEntity sectionEntity = SectionEntity.builder()
                .title(sectionRequest.getTitle())
                .level(sectionRequest.getLevel())
                .topicEntity(topicEntity).build();
        sectionRepository.save(sectionEntity);
        return new ResponseEntity<>(modelMapper.map(sectionEntity, SectionResponse.class), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<SectionResponse>> getAllSections(Long topicId) {
        TopicEntity topicEntity = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic Not Found"));
        if(!topicEntity.getPremiumTopic().equals(1)) {
            List<SectionEntity> sections = sectionRepository.findAllByTopicEntity(topicEntity);
            List<SectionResponse> sectionResponses = sections.stream()
                    .map(sectionEntity -> modelMapper.map(sectionEntity, SectionResponse.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(sectionResponses, HttpStatus.OK);
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This topic is premium, please subscribe to access it.");
        }
    }
    @Override
    public ResponseEntity<List<SectionResponse>> getAllPremiumSections(Long topicId, String username) {
        TopicEntity topicEntity = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic Not Found"));
        if(topicEntity.getPremiumTopic().equals(1)&& getUserPremiumStatus(username).getBody() == 1) {
            List<SectionEntity> sections = sectionRepository.findAllByTopicEntity_Id(topicId);
            List<SectionResponse> sectionResponses = sections.stream()
                    .map(sectionEntity -> modelMapper.map(sectionEntity, SectionResponse.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(sectionResponses, HttpStatus.OK);
        }
        else {
            return getAllSections(topicId);
        }
    }
    private ResponseEntity<Integer> getUserPremiumStatus (String username) {
        String url = "http://user-service/api/users/premium-status/" + username;
        ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Integer premiumStatus = response.getBody();
            if (premiumStatus == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found or premium status not available");
            }
            return response;
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user premium status");
        }
    }
    @Override
    public ResponseEntity<List<SectionResponse>> getSectionsByFilter(SectionFilter filter, Long topicId) {
        try {
            List<SectionEntity> sections = buildFilterQuery(filter, topicId);

            List<SectionResponse> responses = sections.stream()
                    .map(this::mapToSectionResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error filtering sections: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<SectionResponse>> getSectionsByFilterWithAuthenticated(SectionFilter filter, Long topicId, String username) {
        try {
            List<SectionEntity> sections = buildFilterQuery(filter, topicId);

            if (filter.getChallenge_progress() != null && !filter.getChallenge_progress().isEmpty()) {
                sections = filterByUserProgress(sections, username, filter.getChallenge_progress());
            }

            List<SectionResponse> responses = sections.stream()
                    .map(this::mapToSectionResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error filtering sections with authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    private List<SectionEntity> filterByUserProgress(List<SectionEntity> sections, String username, String progressFilter) {
        try {
            String url = "http://user-service/api/user-progress/sections/filter";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("sectionIds", sections.stream().map(SectionEntity::getId).collect(Collectors.toList()));
            requestBody.put("progressFilter", progressFilter);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<Long>>() {}
            );

            List<Long> filteredSectionIds = response.getBody();
            if (filteredSectionIds == null || filteredSectionIds.isEmpty()) {
                return Collections.emptyList();
            }

            return sections.stream()
                    .filter(section -> filteredSectionIds.contains(section.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error filtering by user progress: {}", e.getMessage());
            return sections;
        }
    }
    private List<SectionEntity> buildFilterQuery(SectionFilter filter, Long topicId) {
        return sectionRepository.findSectionsByFilter(
                topicId,
                filter.getLevel(),
                filter.getLessonTitle()
        );
    }

    private SectionResponse mapToSectionResponse(SectionEntity section) {
        return SectionResponse.builder()
                .id(section.getId())
                .title(section.getTitle())
                .level(section.getLevel())
                .build();
    }
}
