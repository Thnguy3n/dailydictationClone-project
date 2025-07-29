package com.example.audioservice.service.Impl;
import com.example.audioservice.entity.ChallengeEntity;
import com.example.audioservice.entity.LessonEntity;
import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.model.Request.LessonRequest;
import com.example.audioservice.model.Response.LessonInfo;
import com.example.audioservice.model.Response.LessonResponse;
import com.example.audioservice.repository.LessonRepository;
import com.example.audioservice.repository.SectionRepository;
import com.example.audioservice.service.LessonService;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final ModelMapper modelMapper;

    @Value("${fireBase_BUCKETNAME}")
    private String BucketName;
    @Override
    public ResponseEntity<List<LessonResponse>> getLessons(Long sectionId) {
        List<LessonEntity> lessonEntities = lessonRepository.findAllBySectionEntity_Id(sectionId);
        if (lessonEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No lessons found for the given section ID");
        }
        List<LessonResponse> lessonResponses = lessonEntities.stream()
                .map(lessonEntity -> {
                    LessonResponse lessonResponse = new LessonResponse();
                    lessonResponse.setId(lessonEntity.getId());
                    lessonResponse.setTitle(lessonEntity.getTitle());
                    if (lessonEntity.getChallengeEntities() != null) {
                        lessonResponse.setCountChallenge(lessonEntity.getChallengeEntities().size());
                    }else {
                        lessonResponse.setCountChallenge(0);
                    }
                    return lessonResponse;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(lessonResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LessonResponse> addLesson(LessonRequest lessonRequest) {
        SectionEntity sectionEntity= sectionRepository.findById(lessonRequest.getSectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Section not found"));
        LessonEntity lessonEntity = LessonEntity.builder()
                .title(lessonRequest.getTitle())
                .audioPath(lessonRequest.getAudioPath())
                .transcript(lessonRequest.getTranscript())
                .sectionEntity(sectionEntity)
                .build();

        lessonRepository.save(lessonEntity);
        return new ResponseEntity<>(modelMapper.map(lessonEntity,LessonResponse.class), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> audioPath(MultipartFile file) throws IOException {
        String bucketName = BucketName;
        String fileName =  UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        Storage storage = StorageClient.getInstance().bucket().getStorage();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/mpeg")
                .setContentDisposition("inline").build();
        storage.create(blobInfo, file.getBytes());

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        String audioUrl = String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                encodedFileName
        );
        return ResponseEntity.ok(audioUrl);
    }

    @Override
    public ResponseEntity<List<LessonInfo>> getAllLessonsWithChallenge() {
        List<LessonEntity> lessonEntities = lessonRepository.findAll();
        if (lessonEntities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<LessonInfo> lessonInfos = lessonEntities.stream()
                .map(lessonEntity -> {
                    LessonInfo lessonInfo = new LessonInfo();
                    lessonInfo.setId(lessonEntity.getId());
                    lessonInfo.setTitle(lessonEntity.getTitle());
                    if (lessonEntity.getChallengeEntities() != null) {
                        List<Long> challengeIds = lessonEntity.getChallengeEntities().stream()
                                .map(ChallengeEntity::getId)
                                .collect(Collectors.toList());
                        lessonInfo.setChallengeIds(challengeIds);
                    }
                    return lessonInfo;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(lessonInfos);
    }

    @Override
    public ResponseEntity<LessonInfo> getLessonInfo(Long lessonId) {
        LessonEntity lessonEntity = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        List<Long> challengeIds = lessonEntity.getChallengeEntities().stream()
                .map(ChallengeEntity::getId)
                .collect(Collectors.toList());
        LessonInfo lessonInfo = new LessonInfo();
        lessonInfo.setId(lessonEntity.getId());
        lessonInfo.setTitle(lessonEntity.getTitle());
        lessonInfo.setChallengeIds(challengeIds);
        return ResponseEntity.ok(lessonInfo);
    }

    @Override
    public ResponseEntity<List<LessonInfo>> getLessonsBySectionId(Long sectionId) {
        try {
            Optional<SectionEntity> sectionOpt = sectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SectionEntity section = sectionOpt.get();

            List<LessonInfo> lessonInfos = section.getLessonEntities().stream()
                    .map(this::convertToLessonInfo)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lessonInfos);

        } catch (Exception e) {
            log.error("Error getting lessons for section {}: {}", sectionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    private LessonInfo convertToLessonInfo(LessonEntity lesson) {
        List<Long> challengeIds = lesson.getChallengeEntities().stream()
                .map(ChallengeEntity::getId)
                .collect(Collectors.toList());
        LessonInfo lessonInfo = new LessonInfo();
        lessonInfo.setId(lesson.getId());
        lessonInfo.setTitle(lesson.getTitle());
        lessonInfo.setChallengeIds(challengeIds);
        return lessonInfo;
    }

}
