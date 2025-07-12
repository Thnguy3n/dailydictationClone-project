package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.ChallengeEntity;
import com.example.audioservice.entity.ChallengeJob;
import com.example.audioservice.entity.LessonEntity;
import com.example.audioservice.entity.TranscriptJob;
import com.example.audioservice.model.DTO.AudioSegment;
import com.example.audioservice.model.DTO.SentenceWithTiming;
import com.example.audioservice.model.DTO.WordData;
import com.example.audioservice.model.DTO.WordInfo;
import com.example.audioservice.model.Request.CheckRequest;
import com.example.audioservice.model.Response.*;
import com.example.audioservice.repository.ChallengeJobRepository;
import com.example.audioservice.repository.ChallengeRepository;
import com.example.audioservice.repository.LessonRepository;
import com.example.audioservice.service.AudioProcessingService;
import com.example.audioservice.service.ChallengeService;
import com.example.audioservice.utils.TextSegmentationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.ColumnTransformer;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final LessonRepository lessonRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final ChallengeJobRepository challengeJobRepository;
    private static final Pattern LINE_PATTERN = Pattern.compile("^(\\d+)\\.\\s*(.*)$");
    private final AudioProcessingService audioProcessingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;
    @Override
    public ResponseEntity<String> addChallenge(String answerKey, Long lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Lesson Not Found"));

        List<ChallengeEntity> challengeEntities = Arrays.stream(answerKey.split("\n"))
                .map(String::trim)
                .filter(line-> line.matches(LINE_PATTERN.pattern().trim()))
                .map(line ->{
                    Matcher matcher = LINE_PATTERN.matcher(line);
                    if(matcher.matches()) {
                        Integer orderIndex = Integer.parseInt(matcher.group(1).trim());
                        String fullSentence = matcher.group(2).trim();
                        String wordData = createWordDataFromSentence(fullSentence);
                        return ChallengeEntity.builder()
                                .fullSentence(fullSentence)
                                .orderIndex(orderIndex)
                                .wordData(wordData)
                                .lesson(lesson)
                                .startTime(0.0) // cần được xử lý
                                .endTime(0.0) // cần được xử lý
                                .build();
                    }
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid challenge format: " + line);
                })
                .collect(Collectors.toList());
        challengeRepository.saveAll(challengeEntities);
        return new ResponseEntity<>("Add challenge successful", HttpStatus.CREATED);
    }

    private String createWordDataFromSentence(String fullSentence) {
        try {
            List<List<String>> wordSegments = TextSegmentationUtil.segmentSentence(fullSentence);
            List<WordInfo> words = new ArrayList<>();
            for (int i = 0; i < wordSegments.size(); i++) {
                WordInfo wordInfo = new WordInfo();
                wordInfo.setIndex(i);
                wordInfo.setAcceptableAnswers(wordSegments.get(i));
                words.add(wordInfo);
            }
            WordData wordData = WordData.builder().words(words).build();

            return objectMapper.writeValueAsString(wordData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating word data for sentence: " + fullSentence, e);
        }
    }

    @Override
    public ResponseEntity<List<ChallengeResponse>> findAllChallengesByLessonId(Long lessonId) {
        List<ChallengeEntity> challengeEntities = challengeRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        if (challengeEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No challenges found for the given lesson ID");
        }
        List<ChallengeResponse> challengeResponses = challengeEntities.stream()
                .map(challengeEntity ->modelMapper.map(challengeEntity,ChallengeResponse.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(challengeResponses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ChallengeInfo>> findChallengesByLessonId(Long lessonId) {
        List<ChallengeEntity> challengeEntities = challengeRepository.findAllByLesson_Id(lessonId);
        if (challengeEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No challenges found for the given lesson ID");
        }
        List<ChallengeInfo> challengeInfos = challengeEntities.stream()
                .map(challengeEntity -> {
                    ChallengeInfo challengeInfo = new ChallengeInfo();
                    challengeInfo.setId(challengeEntity.getId());
                    challengeInfo.setFullSentence(challengeEntity.getFullSentence());
                    challengeInfo.setOrderIndex(challengeEntity.getOrderIndex());
                    challengeInfo.setLessonId(challengeEntity.getLesson().getId());
                    return challengeInfo;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(challengeInfos);
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkAnswer(CheckRequest checkRequest) {
        ChallengeEntity challenge = challengeRepository.findByOrderIndexAndLesson_Id(checkRequest.getOrderIndex(), checkRequest.getLessonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        try {
            WordData wordData = objectMapper.readValue(challenge.getWordData(), WordData.class);
            List<List<String>> wordSegments = wordData.getWords().stream()
                    .map(WordInfo::getAcceptableAnswers)
                    .collect(Collectors.toList());
            Map<String, Object> result = TextSegmentationUtil.getDetailedResult(wordSegments, checkRequest.getUserAnswers());
            result.put("challengeId", challenge.getId());
            result.put("lessonId", checkRequest.getLessonId());
            result.put("fullSentence", challenge.getFullSentence());

            boolean allCorrect = (Boolean) result.get("allCorrect");
            result.put("isPass", allCorrect ? 1 : -1);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (JsonProcessingException  e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error processing challenge data");
        }
    }
  @Override
    public ResponseEntity<Map<String, Object>> checkUserAnswer(CheckRequest checkRequest, String usernameFromToken) {
        Map<String, Object> result = checkAnswer(checkRequest).getBody();
        result.put("username", usernameFromToken);
        try {
            String jobId = UUID.randomUUID().toString();
            String jsonMessage = objectMapper.writeValueAsString(result);
            kafkaTemplate.send("check-user-answer", jobId, jsonMessage);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing message", e);
        }
    }

    @Override
    public ResponseEntity<ChallengeResponse> getFirstChallenge(Long lessonId) {
        ChallengeEntity challengeEntity = challengeRepository.findByLesson_IdAndOrderIndex(lessonId, 1);
        if (challengeEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found");
        }
        ChallengeResponse challengeResponse = modelMapper.map(challengeEntity, ChallengeResponse.class);
        return ResponseEntity.ok(challengeResponse);
    }

    @Override
    public ResponseEntity<ChallengeResponse> continueChallenges(Long lessonId, String username) {
        ProgressResponse progressResponse = getLatestCompleteChallengeDetail(username, lessonId);
        if (progressResponse == null || progressResponse.getChallengeId() == null) {
            return getFirstChallenge(lessonId);
        }
        ChallengeEntity challengeEntity = challengeRepository.findById(progressResponse.getChallengeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        ChallengeResponse challengeResponse = modelMapper.map(challengeEntity, ChallengeResponse.class);
        return ResponseEntity.ok(challengeResponse);
    }

    @Override
    public ResponseEntity<ChallengeResponse> getNextChallenge(Long lessonId, Integer orderIndex) {
        ChallengeEntity challengeEntity = challengeRepository.findByLesson_IdAndOrderIndex(lessonId, orderIndex + 1);
        if (challengeEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Next challenge not found");
        }
        ChallengeResponse challengeResponse = modelMapper.map(challengeEntity, ChallengeResponse.class);
        return ResponseEntity.ok(challengeResponse);
    }

    @Override
    public ResponseEntity<ChallengeResponse> getPreviousChallenge(Long lessonId, Integer orderIndex) {
        ChallengeEntity challengeEntity = challengeRepository.findByLesson_IdAndOrderIndex(lessonId, orderIndex - 1);
        if (challengeEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Previous challenge not found");
        }
        ChallengeResponse challengeResponse = modelMapper.map(challengeEntity, ChallengeResponse.class);
        return ResponseEntity.ok(challengeResponse);
    }

    private ProgressResponse getLatestCompleteChallengeDetail(String username, Long lessonId) {
        try{
            String url = "http://user-service/api/user-progress/last-completed-challenge/" + lessonId + "?username=" + username;
            ResponseEntity<ProgressResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ProgressResponse>() {}
                    );
            return response.getBody();
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving latest challenge detail", e);
        }
    }

    @KafkaListener(topics = "transcript-responses", groupId = "challenge-group" , containerFactory = "challengeKafkaListenerContainerFactory")
    public void handleTranscriptResponse(
            @Header("kafka_receivedMessageKey") String challengeJobId,
            String message)  {
        Optional<ChallengeJob> challengeJob = challengeJobRepository.findByJobId(challengeJobId);
        if (challengeJob.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge job not found");
        }
        if(!"PENDING".equals(challengeJob.get().getStatus())) {
            return;
        }
        updateJobStatus(challengeJobId, "PROCESSING", message, null);
    }

    @Override
    public ResponseEntity<List<ChallengeJobResponse>> processChallenge(String challengeJobId, Long lessonId) {
        try {
            Optional<ChallengeJob> jobOpt = challengeJobRepository.findByJobId(challengeJobId);
            if (jobOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge job not found");
            }
            List<ChallengeEntity> challenges = challengeRepository.findByLesson_Id(lessonId);
            List<String> fullSentences = challenges.stream()
                    .map(ChallengeEntity::getFullSentence)
                    .collect(Collectors.toList());

            AssemblyResponse response = objectMapper.readValue(jobOpt.get().getResponsePayload(), AssemblyResponse.class);
    
            List<AssemblyWordInfoResponse> words = response.getWords();
            Map<String, SentenceWithTiming> matchingSequences = findMatchingSequences(fullSentences, words);

            List<ChallengeJobResponse> results = new ArrayList<>();
            for (ChallengeEntity challenge : challenges) {
                SentenceWithTiming matchingSequence = matchingSequences.get(challenge.getFullSentence());

                if (matchingSequence != null) {
                    challenge.setStartTime(matchingSequence.getStartTime());
                    challenge.setEndTime(matchingSequence.getEndTime());
                    results.add(modelMapper.map(challenge, ChallengeJobResponse.class));
                    challengeRepository.save(challenge);
                    updateJobStatus(challengeJobId, "COMPLETED", "NONE", null);
                } else {
                    updateJobStatus(challengeJobId, "FAILED", null, "No match found for sentence: " + challenge.getFullSentence());
                }
            }
            return new ResponseEntity<>(results, HttpStatus.OK);
        }
        catch (JsonProcessingException e) {
            updateJobStatus(challengeJobId, "FAILED", null, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing challenge job", e);
        }

    }



    private Map<String, SentenceWithTiming> findMatchingSequences(List<String> fullSentences, List<AssemblyWordInfoResponse> words) {
        Map<String, SentenceWithTiming> results = new HashMap<>();

        if (fullSentences == null || fullSentences.isEmpty() || words == null || words.isEmpty()) {
            return results;
        }

        boolean[] usedWords = new boolean[words.size()];

        List<String> sortedSentences = fullSentences.stream()
                .filter(sentence -> sentence != null && !sentence.trim().isEmpty())
                .sorted((s1, s2) -> Integer.compare(normalizeAndSplit(s2).length, normalizeAndSplit(s1).length))
                .collect(Collectors.toList());

        for (String fullSentence : sortedSentences) {
            SentenceWithTiming match = findMatchingSequence(fullSentence, words, usedWords);
            if (match != null) {
                results.put(fullSentence, match);
            }
        }

        return results;
    }

    private SentenceWithTiming findMatchingSequence(String fullSentence, List<AssemblyWordInfoResponse> words, boolean[] usedWords) {
        if (fullSentence == null || fullSentence.trim().isEmpty() || words == null || words.isEmpty()) {
            return null;
        }

        String[] targetWords = normalizeAndSplit(fullSentence);
        if (targetWords.length == 0) {
            return null;
        }

        for (int startIndex = 0; startIndex <= words.size() - targetWords.length; startIndex++) {
            if (isRangeUsed(usedWords, startIndex, startIndex + targetWords.length - 1)) {
                continue;
            }

            if (isSequenceMatch(words, startIndex, targetWords)) {
                int endIndex = startIndex + targetWords.length - 1;

                Double startTime = words.get(startIndex).getStart().doubleValue();
                Double endTime = words.get(endIndex).getEnd().doubleValue();

                List<AssemblyWordInfoResponse> matchedWords = words.subList(startIndex, endIndex + 1);

                String matchedText = matchedWords.stream()
                        .map(AssemblyWordInfoResponse::getText)
                        .collect(Collectors.joining(" "));

                markWordsAsUsed(usedWords, startIndex, endIndex);

                return new SentenceWithTiming(matchedText, startTime, endTime, matchedWords);
            }
        }

        return null;
    }
    private String[] normalizeAndSplit(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .split("\\s+");
    }

    private String normalizeWord(String word) {
        return word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
    }
    private boolean isSequenceMatch(List<AssemblyWordInfoResponse> words, int startIndex, String[] targetWords) {
        if (startIndex + targetWords.length > words.size()) {
            return false;
        }

        for (int i = 0; i < targetWords.length; i++) {
            String transcriptWord = normalizeWord(words.get(startIndex + i).getText());
            String targetWord = targetWords[i];

            if (!transcriptWord.equals(targetWord)) {
                return false;
            }
        }

        return true;
    }
    private boolean isRangeUsed(boolean[] usedWords, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex && i < usedWords.length; i++) {
            if (usedWords[i]) {
                return true;
            }
        }
        return false;
    }
    private void markWordsAsUsed(boolean[] usedWords, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex && i < usedWords.length; i++) {
            usedWords[i] = true;
        }
    }
    private void updateJobStatus(String jobId, String status, String result, String error) {
        Optional<ChallengeJob> jobOpt = challengeJobRepository.findByJobId(jobId);
        if (jobOpt.isPresent()) {
            ChallengeJob job = jobOpt.get();
            if (!result.equals("NONE")){
                job.setStatus(status);
                job.setResponsePayload(result);
                job.setErrorMessage(error);
                challengeJobRepository.save(job);
            }
            job.setStatus(status);
            job.setErrorMessage(error);
            challengeJobRepository.save(job);
        }
        else
        {
            throw new ResourceNotFoundException("Challenge job not found with id: " + jobId);
        }
    }
    @Override
    public ResponseEntity<List<AudioSegmentResponse>> segmentAudioForChallenges(Long lessonId) throws Exception {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        List<ChallengeEntity> challenges = challengeRepository.findByLesson_Id(lessonId);
        if (challenges.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No challenges found for lesson");
        }
        List<AudioSegment> segments = challenges.stream()
                .map(challenge -> AudioSegment.builder()
                        .challengeId(challenge.getId())
                        .lessonId(lessonId)
                        .orderIndex(challenge.getOrderIndex())
                        .fullSentence(challenge.getFullSentence())
                        .startTime(challenge.getStartTime())
                        .endTime(challenge.getEndTime())
                        .fileName(generateFileName(challenge))
                        .build())
                .collect(Collectors.toList());
        List<AudioSegmentResponse> responses = audioProcessingService.segmentAudio(
                lesson.getAudioPath(), segments
        );
        for (AudioSegmentResponse response : responses) {
            if ("SUCCESS".equals(response.getStatus())) {
                ChallengeEntity challenge = challengeRepository.findById(response.getChallengeId())
                        .orElse(null);
                if (challenge != null) {
                    challenge.setAudioSegmentUrl(response.getAudioUrl());
                    challengeRepository.save(challenge);
                }
            }
        }
        return ResponseEntity.ok(responses);
    }
    private String generateFileName(ChallengeEntity challenge) {
        return String.format("challenge_%d_%s",
                challenge.getId(),
                challenge.getFullSentence().replaceAll("[^a-zA-Z0-9]", "_"));
    }
}
