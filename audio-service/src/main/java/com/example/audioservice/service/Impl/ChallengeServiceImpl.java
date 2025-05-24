package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.ChallengeEntity;
import com.example.audioservice.entity.LessonEntity;
import com.example.audioservice.model.Response.ChallengeResponse;
import com.example.audioservice.repository.ChallengeRepository;
import com.example.audioservice.repository.LessonRepository;
import com.example.audioservice.service.ChallengeService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final LessonRepository lessonRepository;
    private final ModelMapper modelMapper;
    public ChallengeServiceImpl(ChallengeRepository challengeRepository, LessonRepository lessonRepository, ModelMapper modelMapper) {
        this.challengeRepository = challengeRepository;
        this.lessonRepository = lessonRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public ResponseEntity<String> addChallenge(String answerKey, Long lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Lesson Not Found"));
        List<ChallengeEntity> challengeEntities= Arrays.stream(answerKey.split("\n"))
                .map(line -> line.replaceAll("^\\d+\\.\\s*", ""))
                .filter(content -> !content.isBlank())
                .map(content -> ChallengeEntity.builder().answerKey(content).isPass(0).lesson(lesson).build())
                .collect(Collectors.toList());
        challengeRepository.saveAll(challengeEntities);
        return new ResponseEntity<>("Add challenge successful", HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<List<ChallengeResponse>> findAllChallengesByLessonId(Long lessonId) {
        List<ChallengeEntity> challengeEntities = challengeRepository.findByLesson_Id(lessonId);
        List<ChallengeResponse> challengeResponses = challengeEntities.stream()
                .map(challengeEntity ->modelMapper.map(challengeEntity,ChallengeResponse.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(challengeResponses, HttpStatus.OK);
    }

}
