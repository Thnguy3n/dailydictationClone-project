package com.example.audioservice.service.Impl;
import com.example.audioservice.entity.LessonEntity;
import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.model.Request.LessonRequest;
import com.example.audioservice.model.Response.LessonResponse;
import com.example.audioservice.repository.LessonRepository;
import com.example.audioservice.repository.SectionRepository;
import com.example.audioservice.service.LessonService;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final ModelMapper modelMapper;
    public LessonServiceImpl(LessonRepository lessonRepository, SectionRepository sectionRepository, ModelMapper modelMapper) {
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.modelMapper = modelMapper;
    }
    @Value("${fireBase_BUCKETNAME}")
    private String BucketName;
    @Override
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        List<LessonEntity> lessonEntities = lessonRepository.findAll();
        List<LessonResponse> lessonResponses = lessonEntities.stream()
                .map(lessonEntity -> modelMapper.map(lessonEntity,LessonResponse.class))
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


}
