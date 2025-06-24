package com.example.audioservice.repository;

import com.example.audioservice.entity.TranscriptJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranscriptJobRepository extends JpaRepository<TranscriptJob, Long> {
    Optional<TranscriptJob> findByJobId(String jobId);
}
