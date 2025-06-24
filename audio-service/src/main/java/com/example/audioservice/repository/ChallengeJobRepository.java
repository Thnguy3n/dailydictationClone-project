package com.example.audioservice.repository;

import com.example.audioservice.entity.ChallengeJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeJobRepository extends JpaRepository<ChallengeJob,Long> {
    Optional<ChallengeJob>  findByJobId(String jobId);
}
