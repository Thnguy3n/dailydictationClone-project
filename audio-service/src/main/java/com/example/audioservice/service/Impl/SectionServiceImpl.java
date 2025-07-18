package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.entity.TopicEntity;
import com.example.audioservice.model.Request.SectionRequest;
import com.example.audioservice.model.Response.SectionResponse;
import com.example.audioservice.repository.SectionRepository;
import com.example.audioservice.repository.TopicRepository;
import com.example.audioservice.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
}
