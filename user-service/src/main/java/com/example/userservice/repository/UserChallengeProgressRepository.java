package com.example.userservice.repository;

import com.example.userservice.entity.UserChallengeProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeProgressRepository extends JpaRepository<UserChallengeProgressEntity,Long> {
}
