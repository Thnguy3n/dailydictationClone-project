package com.example.audioservice.service.Impl;

import com.example.audioservice.model.DTO.AudioSegment;
import com.example.audioservice.model.Response.AudioSegmentResponse;
import com.example.audioservice.service.AudioProcessingService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.firebase.cloud.StorageClient;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AudioProcessingServiceImpl implements AudioProcessingService {
    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${ffprobe.path:/usr/bin/ffprobe}")
    private String ffprobePath;

    @Value("${temp.audio.directory:/tmp/audio}")
    private String tempAudioDirectory;

    @Value("${fireBase_BUCKETNAME}")
    private String bucketName;

    @Override
    public List<AudioSegmentResponse> segmentAudio(String audioUrl, List<AudioSegment> segments) throws Exception {
        List<AudioSegmentResponse> responses = new ArrayList<>();

        // Tạo thư mục temp nếu chưa tồn tại
        Path tempDir = Paths.get(tempAudioDirectory);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Download audio từ Firebase
        String localAudioPath = downloadAudioFromFirebase(audioUrl);

        try {
            // Khởi tạo FFmpeg
            log.debug("Initializing FFmpeg with path: {} and FFprobe with path: {}", ffmpegPath, ffprobePath);
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffprobePath);
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            // Xử lý từng segment
            for (AudioSegment segment : segments) {
                try {
                    AudioSegmentResponse response = processAudioSegment(
                            executor, localAudioPath, segment
                    );
                    responses.add(response);
                } catch (Exception e) {
                    log.error("Error processing segment for challenge {}: {}",
                            segment.getChallengeId(), e.getMessage());

                    AudioSegmentResponse errorResponse = AudioSegmentResponse.builder()
                            .challengeId(segment.getChallengeId())
                            .fullSentence(segment.getFullSentence())
                            .status("ERROR")
                            .error(e.getMessage())
                            .build();
                    responses.add(errorResponse);
                }
            }

        } finally {
            cleanupTempFile(localAudioPath);
        }
        return responses;
    }
    private AudioSegmentResponse processAudioSegment(FFmpegExecutor executor,
                                                     String inputPath,
                                                     AudioSegment segment) throws Exception {

        // Tạo tên file output
       String outputFileName = String.format("challenge_%d_%d.mp3", segment.getLessonId(), segment.getOrderIndex());
        String outputPath = Paths.get(tempAudioDirectory, outputFileName).toString();

        try {
            // Convert milliseconds to seconds for FFmpeg
            double startSeconds = segment.getStartTime() / 1000.0;
            double durationSeconds = (segment.getEndTime() - segment.getStartTime()) / 1000.0;

            // Build FFmpeg command
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath)
                    .overrideOutputFiles(true)
                    .addOutput(outputPath)
                    .setFormat("mp3")
                    .setAudioCodec("mp3")
                    .setAudioBitRate(128000)
                    .setAudioSampleRate(44100)
                    .setStartOffset((long)(startSeconds * 1000), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setDuration((long)(durationSeconds * 1000), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .done();

            // Execute FFmpeg command
            executor.createJob(builder).run();

            // Upload segment to Firebase
            String segmentUrl = uploadSegmentToFirebase(outputPath, outputFileName);

            return AudioSegmentResponse.builder()
                    .challengeId(segment.getChallengeId())
                    .fullSentence(segment.getFullSentence())
                    .audioUrl(segmentUrl)
                    .startTime(segment.getStartTime())
                    .endTime(segment.getEndTime())
                    .status("SUCCESS")
                    .build();

        } finally {
            // Cleanup temp segment file
            cleanupTempFile(outputPath);
        }
    }

    @Override
    public String downloadAudioFromFirebase(String firebaseUrl) throws Exception {
        log.info("Downloading audio from: {}", firebaseUrl);

        // Tạo tên file temp
        String tempFileName = "temp_audio_" + UUID.randomUUID() + ".mp3";
        String tempFilePath = Paths.get(tempAudioDirectory, tempFileName).toString();

        try (InputStream inputStream = new URL(firebaseUrl).openStream()) {
            Files.copy(inputStream, Paths.get(tempFilePath), StandardCopyOption.REPLACE_EXISTING);
            log.info("Audio downloaded to: {}", tempFilePath);
            return tempFilePath;
        }
    }

    @Override
    public String uploadSegmentToFirebase(String segmentFilePath, String fileName) throws Exception {
        log.info("Uploading segment to Firebase: {}", fileName);

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        // Tạo path cho segment trong Firebase
        String firebaseFileName = "audio_segments/" + fileName;

        BlobId blobId = BlobId.of(bucketName, firebaseFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/mpeg")
                .setContentDisposition("inline")
                .build();

        byte[] fileBytes = Files.readAllBytes(Paths.get(segmentFilePath));
        storage.create(blobInfo, fileBytes);

        String encodedFileName = URLEncoder.encode(firebaseFileName, StandardCharsets.UTF_8);
        String audioUrl = String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                encodedFileName
        );

        log.info("Segment uploaded successfully: {}", audioUrl);
        return audioUrl;
    }
    private String generateSegmentFileName(AudioSegment segment) {
        String sanitizedSentence = segment.getFullSentence()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .substring(0, Math.min(50, segment.getFullSentence().length()));

        return String.format("challenge_%d_%s_%d.mp3",
                segment.getChallengeId(),
                sanitizedSentence,
                System.currentTimeMillis());
    }
    private void cleanupTempFile(String filePath) {
        try {
            if (filePath != null && Files.exists(Paths.get(filePath))) {
                Files.delete(Paths.get(filePath));
                log.debug("Cleaned up temp file: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup temp file {}: {}", filePath, e.getMessage());
        }
    }
}
