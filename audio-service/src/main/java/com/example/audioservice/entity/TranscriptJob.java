package com.example.audioservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "transcriptJobs")
@AllArgsConstructor
public class TranscriptJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String jobId;

    @Column(nullable = false)
    private String audioUrl;

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, ERROR

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column
    private String error;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public TranscriptJob() {
    }
}