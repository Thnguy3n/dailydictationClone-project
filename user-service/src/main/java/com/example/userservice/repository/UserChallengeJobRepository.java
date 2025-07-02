package com.example.userservice.repository;

import com.example.userservice.entity.UserChallengeProgressEntity;
import com.example.userservice.entity.UserChallengeProgressJobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChallengeJobRepository extends JpaRepository<UserChallengeProgressJobs,Long> {
    Optional<UserChallengeProgressJobs> findByJobId(String jobId);
}
