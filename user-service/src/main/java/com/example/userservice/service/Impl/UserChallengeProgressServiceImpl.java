package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserChallengeProgressEntity;
import com.example.userservice.entity.UserChallengeProgressJobs;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.response.*;
import com.example.userservice.repository.UserChallengeJobRepository;
import com.example.userservice.repository.UserChallengeProgressRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserChallengeProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserChallengeProgressServiceImpl implements UserChallengeProgressService {
   private final UserRepository userRepository;
   private final UserChallengeJobRepository userChallengeJobRepository;
   private final UserChallengeProgressRepository userChallengeProgressRepository;
   private final ObjectMapper objectMapper;
   private final RestTemplate restTemplate;

    @KafkaListener(topics = "check-user-answer", groupId = "user-challenge-progress-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void userCompletedChallenge(
            @Header ("kafka_receivedMessageKey") String jobId,
            String message){
        Optional<UserChallengeProgressJobs> existingJob = userChallengeJobRepository.findByJobId(jobId);
        if (existingJob.isPresent() &&
                (existingJob.get().getStatus().equals("COMPLETED") ||
                        existingJob.get().getStatus().equals("FAILED"))) {
            return ;
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
                .createdAt(LocalDateTime.now())
                .build();
        userChallengeJobRepository.save(userChallengeProgressJobs);
        try {
            Map<String, Object> resultMap = objectMapper.readValue(message, Map.class);
            String username = (String) resultMap.get("username");
            Long challengeId = Long.valueOf(resultMap.get("challengeId").toString());
            Long lessonId = Long.valueOf(resultMap.get("lessonId").toString());
            Integer isPass = (Integer) resultMap.get("isPass");
            UserEntity userEntity = userRepository.findByUsername(username);
            if (userEntity == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }
            UserChallengeProgressEntity existingProgress = userChallengeProgressRepository
                    .findByChallengeIdAndUserId(challengeId, userEntity.getId());

            if (existingProgress == null) {
                UserChallengeProgressEntity newProgress = UserChallengeProgressEntity.builder()
                        .challengeId(challengeId)
                        .lessonId(lessonId)
                        .userId(userEntity.getId())
                        .isCompleted(isPass)
                        .totalAttempts(1)
                        .firstAttemptAt(LocalDateTime.now())
                        .completedAt(isPass == 1 ? LocalDateTime.now() : null)
                        .lastAttemptAt(LocalDateTime.now())
                        .build();
                userChallengeProgressRepository.save(newProgress);
            } else {
                if (existingProgress.getIsCompleted() == -1) {
                    existingProgress.setTotalAttempts(existingProgress.getTotalAttempts() + 1);
                    existingProgress.setLastAttemptAt(LocalDateTime.now());

                    if (isPass == 1) {
                        existingProgress.setIsCompleted(1);
                        existingProgress.setCompletedAt(LocalDateTime.now());
                    }
                    userChallengeProgressRepository.save(existingProgress);
                }
                else {
                    log.info("User {} has already completed challenge {}. No further action needed.", username, challengeId);
                }
            }
            userChallengeProgressJobs.setStatus("COMPLETED");
            userChallengeProgressJobs.setUpdatedAt(LocalDateTime.now());
            userChallengeJobRepository.save(userChallengeProgressJobs);

        }catch (Exception e){
            userChallengeProgressJobs.setStatus("FAILED");
            userChallengeProgressJobs.setUpdatedAt(LocalDateTime.now());
            userChallengeProgressJobs.setErrorMessage(e.getMessage());
            userChallengeJobRepository.save(userChallengeProgressJobs);
        }
    }

    @Override
    public ResponseEntity<List<UserLessonProgressResponse>> getUserLessonProgress(String username) {
        try {
            UserEntity userEntity = userRepository.findByUsername(username);
            if (userEntity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            List<LessonInfo> allLessons = getAllLessonsFromAudioService();
            List<UserLessonProgressResponse> progressList = new ArrayList<>();

            for (LessonInfo lesson : allLessons) {
                UserLessonProgressResponse progress = calculateLessonProgress(userEntity.getId(), lesson);
                progressList.add(progress);
            }
            progressList.sort((a, b) -> {
                if (!Objects.equals(a.getProgressPercentage(), b.getProgressPercentage())) {
                    return Double.compare(b.getProgressPercentage(), a.getProgressPercentage());
                }
                if (a.getLastAccessedAt() == null && b.getLastAccessedAt() == null) return 0;
                if (a.getLastAccessedAt() == null) return 1;
                if (b.getLastAccessedAt() == null) return -1;
                return b.getLastAccessedAt().compareTo(a.getLastAccessedAt());
            });
            return ResponseEntity.ok(progressList);
        }catch (Exception e ){
            log.error("Error fetching user lesson progress for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<UserLessonProgressDetailResponse> getUserLessonProgressDetail(String username, Long lessonId) {
        try{
            UserEntity user = userRepository.findByUsername(username);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
            LessonInfo lessonInfo = getLessonInfoFromAudioService(lessonId);
            List<ChallengeInfo> challenges = getChallengesFromAudioService(lessonId);
            UserLessonProgressResponse lessonProgress = calculateLessonProgress(user.getId(), lessonInfo);
            List<UserChallengeDetailResponse> challengeDetails = new ArrayList<>();
            for (ChallengeInfo challenge : challenges) {
                UserChallengeDetailResponse detail = calculateChallengeDetail(user.getId(), challenge);
                challengeDetails.add(detail);
            }
            challengeDetails.sort(Comparator.comparing(UserChallengeDetailResponse::getOrderIndex));

            UserLessonProgressDetailResponse response = UserLessonProgressDetailResponse.builder()
                    .lessonProgress(lessonProgress)
                    .challengeDetails(challengeDetails)
                    .build();

            return ResponseEntity.ok(response);
        }catch (Exception e){
            log.error("Error fetching user lesson progress detail for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ProgressResponse> getLatestCompleteChallengeDetail(String username, Long lessonId) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        UserChallengeProgressEntity userChallengeProgressEntity = userChallengeProgressRepository.findFirstByUserIdAndLessonIdAndIsCompletedOrderByCompletedAtDesc(user.getId(),lessonId, 1);
        if (userChallengeProgressEntity == null) {
            return ResponseEntity.ok(null);
        }
       ProgressResponse progressResponse = ProgressResponse.builder()
                .lessonId(lessonId)
                .challengeId(userChallengeProgressEntity.getChallengeId())
                .build();

        return ResponseEntity.ok(progressResponse);
    }

    private UserChallengeDetailResponse calculateChallengeDetail(String userId, ChallengeInfo challengeInfo) {
        UserChallengeProgressEntity progress = userChallengeProgressRepository
                .findByChallengeIdAndUserId(challengeInfo.getId(), userId);
        if (progress == null) {
            return UserChallengeDetailResponse.builder()
                    .challengeId(challengeInfo.getId())
                    .fullSentence(challengeInfo.getFullSentence())
                    .orderIndex(challengeInfo.getOrderIndex())
                    .isCompleted(0)
                    .attempts(0)
                    .completedAt(null)
                    .currentlyPassed(false)
                    .build();
        }
        return UserChallengeDetailResponse.builder()
                .challengeId(challengeInfo.getId())
                .fullSentence(challengeInfo.getFullSentence())
                .orderIndex(challengeInfo.getOrderIndex())
                .isCompleted(progress.getIsCompleted())
                .attempts(progress.getTotalAttempts())
                .completedAt(progress.getCompletedAt())
                .currentlyPassed(progress.getIsCompleted() == 1)
                .build();
    }

    private List<ChallengeInfo> getChallengesFromAudioService(Long lessonId) {
        try {
            String url = "http://audio-service/api/challenge/lesson/" + lessonId + "/info";
            ResponseEntity<List<ChallengeInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ChallengeInfo>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling audio service to get challenges for lessonId: {}", lessonId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch challenges");
        }
    }

    private LessonInfo getLessonInfoFromAudioService(Long lessonId) {
        try {
            String url = "http://audio-service/api/lessons/" + lessonId +"/info";
            ResponseEntity<LessonInfo> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<LessonInfo>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling audio service to get lesson info for lessonId: {}", lessonId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch lesson info");
        }
    }

    private List<LessonInfo> getAllLessonsFromAudioService() {
        try {
            String url = "http://audio-service/api/lessons/all-challenge";
            ResponseEntity<List<LessonInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<LessonInfo>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling audio service to get all lessons", e);
            return Collections.emptyList();
        }
    }
    private UserLessonProgressResponse calculateLessonProgress(String id, LessonInfo lessonInfo) {
        List<Long> challengeIds = lessonInfo.getChallengeIds();
        if (challengeIds == null || challengeIds.isEmpty()) {
            return UserLessonProgressResponse.builder()
                    .lessonId(lessonInfo.getId())
                    .lessonTitle(lessonInfo.getTitle())
                    .totalChallenges(0)
                    .attemptedChallenges(0)
                    .passedChallenges(0)
                    .progressPercentage(0.0)
                    .passPercentage(0.0)
                    .status("NOT_STARTED")
                    .build();
        }
        Long attemptedChallenges  = userChallengeProgressRepository.countDistinctChallengeIdByUserIdAndChallengeIdIn(id,challengeIds);
        Long completedChallenges = userChallengeProgressRepository.countDistinctByUserIdAndIsCompletedAndChallengeIdIn(id,1,challengeIds);
        int totalChallenges = challengeIds.size();

        Optional<UserChallengeProgressEntity> scope = userChallengeProgressRepository.findFirstByUserIdAndChallengeIdInOrderByLastAttemptAtDesc(id, challengeIds);
        Optional<LocalDateTime> lastAccessedAt = scope.map(UserChallengeProgressEntity::getLastAttemptAt);

        double progressPercentage = totalChallenges > 0 ? (attemptedChallenges.doubleValue() / totalChallenges) * 100 : 0.0;
        double passPercentage = totalChallenges > 0 ? (completedChallenges.doubleValue() / totalChallenges) * 100 : 0.0;
        String status;
        LocalDateTime completedAt = null;

        if (attemptedChallenges == 0) {
            status = "NOT_STARTED";
        } else if (attemptedChallenges.equals((long) totalChallenges) && completedChallenges.equals((long) totalChallenges)) {
            status = "COMPLETED";
            completedAt = userChallengeProgressRepository
                    .findFirstByUserIdAndChallengeIdInAndIsCompletedOrderByCompletedAtDesc(id, challengeIds, 1)
                    .map(UserChallengeProgressEntity::getCompletedAt)
                    .orElse(null);
        } else {
            status = "IN_PROGRESS";
        }
        return UserLessonProgressResponse.builder()
                .lessonId(lessonInfo.getId())
                .lessonTitle(lessonInfo.getTitle())
                .totalChallenges(totalChallenges)
                .attemptedChallenges(attemptedChallenges.intValue())
                .passedChallenges(completedChallenges.intValue())
                .progressPercentage(Math.round(progressPercentage * 100.0) / 100.0)
                .passPercentage(Math.round(passPercentage * 100.0) / 100.0)
                .status(status)
                .lastAccessedAt(lastAccessedAt.orElse(null))
                .completedAt(completedAt)
                .build();
    }
}
