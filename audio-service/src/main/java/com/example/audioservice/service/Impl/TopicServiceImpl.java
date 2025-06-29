package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.CategoryEntity;
import com.example.audioservice.entity.TopicEntity;
import com.example.audioservice.model.Request.TopicRequest;
import com.example.audioservice.model.Response.TopicResponse;
import com.example.audioservice.repository.CategoryRepository;
import com.example.audioservice.repository.TopicRepository;
import com.example.audioservice.service.TopicService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Stream.builder;


@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {
    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    @Value("${fireBase_BUCKETNAME}")
    private String BucketName;

    @Override
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        List<TopicEntity> topicEntities = topicRepository.findAll();
        List<TopicResponse> topicResponses = topicEntities.stream().map(t->modelMapper.map(t, TopicResponse.class)).collect(Collectors.toList());
        return new ResponseEntity<>(topicResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TopicResponse> addTopic(TopicRequest topicRequest) {
        CategoryEntity categoryEntity = categoryRepository.findById(topicRequest.getCategoryId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Category not found"));
            TopicEntity topicEntity = TopicEntity.builder()
                    .title(topicRequest.getTitle())
                    .levels(topicRequest.getLevels())
                    .description(topicRequest.getDescription())
                    .categoryEntity(categoryEntity)
                    .image(topicRequest.getImage()).build();
            topicRepository.save(topicEntity);
            return new ResponseEntity<>(modelMapper.map(topicEntity, TopicResponse.class), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null|| !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("File is not an image");
        }
        try {
            String bucketName = BucketName;
            String filename ="TopicImage/"+ UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Storage storage = StorageClient.getInstance().bucket().getStorage();

            BlobId blobId = BlobId.of(bucketName, filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());
            String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            String imageUrl  = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucketName,
                    encodedFileName
            );
            return ResponseEntity.ok(imageUrl);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", e);
        }
    }
}
