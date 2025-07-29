package com.example.userservice.repository;

import com.example.userservice.constants.OtpType;
import com.example.userservice.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity,Long> {

    int countByEmailAndCreatedAtAfter(String email, LocalDateTime createdAtAfter);

    void deleteByEmailAndOtpType(String email, OtpType otpType);

    Optional<OtpEntity> findByEmailAndOtpCodeAndUsedAndExpiresAtAfter(String email, String otpCode, boolean used, LocalDateTime expiresAtAfter);
}
