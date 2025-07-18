package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.ChallengeJob;
import com.example.audioservice.entity.TranscriptJob;
import com.example.audioservice.model.DTO.TranscriptMessage;
import com.example.audioservice.model.Request.TranscriptRequest;
import com.example.audioservice.model.Response.AssemblyResponse;
import com.example.audioservice.model.Response.AssemblyWordInfoResponse;
import com.example.audioservice.model.Response.ProgressResponse;
import com.example.audioservice.model.Response.TranscriptJobResponse;
import com.example.audioservice.repository.ChallengeJobRepository;
import com.example.audioservice.repository.TranscriptJobRepository;
import com.example.audioservice.service.TranscriptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {
    @Value("${assemblyAI_API_KEY}")
    private String API_KEY;
    @Value("${assemblyAI_TRANSCRIPT_URL}")
    private String TRANSCRIPT_URL;

    private String transcriptRequestTopic = "transcript-requests";
    private static final HttpClient client = HttpClient.newHttpClient();

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TranscriptJobRepository jobRepository;
    private final ChallengeJobRepository challengeJobRepository;
    private final RestTemplate restTemplate;

    @Override
    public List<String> submitTranscriptJob(TranscriptRequest transcriptRequest) {
        String transcriptJobId = UUID.randomUUID().toString();
        String challengeJobId = UUID.randomUUID().toString();
        List<String> results = List.of(
                transcriptJobId,challengeJobId
        );
        TranscriptJob job = new TranscriptJob();
        job.setJobId(transcriptJobId);
        job.setAudioUrl(transcriptRequest.getAudioUrl());
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        jobRepository.save(job);

        TranscriptMessage message = new TranscriptMessage(transcriptJobId, transcriptRequest.getAudioUrl(), challengeJobId);

        try {
            kafkaTemplate.send(transcriptRequestTopic, transcriptJobId, objectToJson(message))
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            updateJobStatus(transcriptJobId, "ERROR", null, "Failed to send message to Kafka: " + ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            updateJobStatus(transcriptJobId, "ERROR", null, "Failed to submit job: " + e.getMessage());
            throw new RuntimeException("Failed to submit transcript job", e);
        }
        return results;
    }
    private String objectToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON conversion failed for object: " + object, e);
        }
    }

    @Override
    public TranscriptJobResponse getJobStatus(String jobId) {
        return jobRepository.findByJobId(jobId)
                .map(this::toTranscriptJobResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript job not found with id: " + jobId));
    }

    private TranscriptJobResponse toTranscriptJobResponse(TranscriptJob job) {
        return TranscriptJobResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .result(job.getResult())
                .error(job.getError())
                .build();
    }
    @Override
    public String getJobResult(String jobId) {
        TranscriptJobResponse status = getJobStatus(jobId);
        return (status != null && status.getStatus().equals("COMPLETED")) ? status.getResult() : null;
    }

    @KafkaListener(topics = "transcript-requests", groupId = "transcript-group", containerFactory = "kafkaListenerContainerFactory")
    public void processTranscriptRequest(String message, @Header("kafka_receivedMessageKey") String jobId) {
        try {
            Optional<TranscriptJob> jobOpt = jobRepository.findByJobId(jobId);
            if (!jobOpt.isPresent()) {
                return;
            }
            TranscriptJob job = jobOpt.get();
            if (!"PENDING".equals(job.getStatus())) {
                return;
            }
            TranscriptMessage transcriptMessage = jsonToObject(message, TranscriptMessage.class);

            updateJobStatus(jobId, "PROCESSING", null, null);

            AssemblyResponse result = processTranscription(transcriptMessage.getAudioUrl());
            String challengeJobId = transcriptMessage.getChallengeJobId();
            ChallengeJob challengeJob = ChallengeJob.builder()
                    .jobId(challengeJobId)
                    .responsePayload(objectToJson(result))
                    .processedAt(Instant.now())
                    .status("PENDING")
                    .build();
            challengeJobRepository.save(challengeJob);

            kafkaTemplate.send("transcript-responses", challengeJobId, objectToJson(result))
                    .whenComplete((sendResult, ex) -> {
                        if (ex != null) {
                            updateJobStatus(jobId, "ERROR", null, "Failed to send response to Kafka: " + ex.getMessage());
                        }
                    });
            updateJobStatus(jobId, "COMPLETED", result.getText(), null);

        } catch (Exception e) {
            updateJobStatus(jobId, "ERROR", null, e.getMessage());
            System.err.println("Error processing transcript request for jobId: " + jobId + ", error: " + e.getMessage());
        }
    }

    private AssemblyResponse processTranscription(String audioUrl) throws Exception {
        String transcriptId = createTranscript(audioUrl);
        return getTranscriptResult(transcriptId);
    }

    private <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON conversion failed for object: " + clazz, e);
        }
    }

    private void updateJobStatus(String jobId, String status, String result, String error) {
        Optional<TranscriptJob> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isPresent()) {
            TranscriptJob job = jobOpt.get();
            job.setStatus(status);
            job.setResult(result);
            job.setError(error);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
        else
        {
            throw new ResourceNotFoundException("Transcript job not found with id: " + jobId);
        }
    }

    private String createTranscript(String audioUrl) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio_url", audioUrl);

        requestBody.put("format_text", true);
        requestBody.put("punctuate", true);
        requestBody.put("dual_channel", false);
        requestBody.put("webhook_url",null);

        requestBody.put("word_boost",new String[]{});
        requestBody.put("boost_param","default");

        requestBody.put("disfluencies", false);
        requestBody.put("filter_profanity", false);
        requestBody.put("redact_pii", false);


        requestBody.put("speaker_labels", false);
        requestBody.put("auto_chapters", false);
        requestBody.put("auto_highlights", false);
        requestBody.put("sentiment_analysis", false);
        requestBody.put("entity_detection", false);
        requestBody.put("iab_categories", false);
        requestBody.put("content_safety", false);

        String jsonBody = objectToJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSCRIPT_URL))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseJson(response.body(), "id");
        }
        throw new RuntimeException("Error creating transcript: " + response.body());
    }

    private static String parseJson(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return null;

            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;

            int valueStartIndex = json.indexOf("\"", colonIndex);
            if (valueStartIndex == -1) return null;

            int valueEndIndex = json.indexOf("\"", valueStartIndex + 1);
            if (valueEndIndex == -1) return null;

            return json.substring(valueStartIndex + 1, valueEndIndex);
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            System.out.println("JSON string: " + json);
            return null;
        }
    }
    private AssemblyResponse getTranscriptResult(String transcriptId) throws Exception {
        String status = "";
        String resultUrl = TRANSCRIPT_URL + "/" + transcriptId;

        HttpResponse<String> response = null;
        int pollCount = 0;
        int maxPoll = 60;
        while (!status.equals("completed") && pollCount < maxPoll) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resultUrl))
                    .header("Authorization", API_KEY)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error getting transcript: " + response.body());
            }

            status = parseJson(response.body(), "status");

            if (status.equals("error")) {
                throw new RuntimeException("Transcription failed: " + parseJson(response.body(), "error"));
            }

            if (!status.equals("completed")) {
                Thread.sleep(5000);
                pollCount++;
            }
        }
        if (!status.equals("completed")) {
            throw new RuntimeException("Transcription timeout after " + maxPoll + " polling attempts");
        }
        return AssemblyResponse.builder()
                .text(parseJson(response.body(), "text"))
                .words(parseWords(response.body()))
                .build();
    }
    private List<AssemblyWordInfoResponse> parseWords(String json) {
        List<AssemblyWordInfoResponse> words = new ArrayList<>();
        try {
            for(JsonNode node : new ObjectMapper().readTree(json).get("words")) {
                AssemblyWordInfoResponse wordInfo = new AssemblyWordInfoResponse();
                wordInfo.setText(node.get("text").asText());
                wordInfo.setStart(node.get("start").asInt());
                wordInfo.setEnd(node.get("end").asInt());
                wordInfo.setConfidence(node.get("confidence").asDouble());
                words.add(wordInfo);
            }
            return words;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing words from JSON: " + e.getMessage());
        }
    }

}
