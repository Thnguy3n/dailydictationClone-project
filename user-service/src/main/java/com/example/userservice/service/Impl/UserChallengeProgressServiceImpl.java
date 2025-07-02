package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserChallengeProgressEntity;
import com.example.userservice.entity.UserChallengeProgressJobs;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserChallengeJobRepository;
import com.example.userservice.repository.UserChallengeProgressRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserChallengeProgressService;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserChallengeProgressServiceImpl implements UserChallengeProgressService {
   private final UserRepository userRepository;
   private final UserChallengeJobRepository userChallengeJobRepository;
   private final UserChallengeProgressRepository userChallengeProgressRepository;
   private final ObjectMapper objectMapper;
    @KafkaListener(topics = "check-user-answer", groupId = "user-challenge-progress-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void userCompletedChallenge(
            @Header ("kafka_receivedMessageKey") String jobId,
            String message){
        Optional<UserChallengeProgressJobs> existingJob = userChallengeJobRepository.findByJobId(jobId);
        if (existingJob.isPresent() &&
                (existingJob.get().getStatus().equals("COMPLETED") ||
                        existingJob.get().getStatus().equals("FAILED"))) {
            return;
        }

        if (existingJob.isEmpty()) {
            processUserChallengeProgress(jobId, message);
        }
    }
    public void processUserChallengeProgress(String jobId, String message) {
        UserChallengeProgressJobs userChallengeProgressJobs = UserChallengeProgressJobs.builder()
                .jobId(jobId)
                .status("PENDING")
                .result(message)
                .errorMessage(null)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        userChallengeJobRepository.save(userChallengeProgressJobs);
        try {
            Map<String, Object> resultMap = objectMapper.readValue(message, Map.class);
            String username = (String) resultMap.get("username");
            Long challengeId = Long.valueOf(resultMap.get("challengeId").toString());
            Integer isPass = (Integer) resultMap.get("isPass");
            log.debug("Parsed message - username: {}, challengeId: {}, isPass: {}",
                    username, challengeId, isPass);
            UserEntity userEntity = userRepository.findByUsername(username);
            UserChallengeProgressEntity userChallengeProgressEntity= UserChallengeProgressEntity.builder()
                    .challengeId(challengeId)
                    .userId(userEntity.getId())
                    .isCompleted(isPass)
                    .completedAt(LocalDateTime.now())
                    .build();
            userChallengeProgressRepository.save(userChallengeProgressEntity);
            userChallengeProgressJobs.setStatus("COMPLETED");
            userChallengeProgressJobs.setUpdatedAt(LocalDateTime.now());
            userChallengeJobRepository.save(userChallengeProgressJobs);

        }
        catch (Exception e){
            userChallengeProgressJobs.setStatus("FAILED");
            userChallengeProgressJobs.setUpdatedAt(LocalDateTime.now());
            userChallengeProgressJobs.setErrorMessage(e.getMessage());
            userChallengeJobRepository.save(userChallengeProgressJobs);
        }
    }
}
