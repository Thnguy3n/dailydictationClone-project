package com.example.userservice.service.Impl;

import com.example.userservice.entity.OtpEntity;
import com.example.userservice.model.request.SendOtpRequest;
import com.example.userservice.model.request.VerifyOtpRequest;
import com.example.userservice.model.response.OtpResponse;
import com.example.userservice.repository.OtpRepository;
import com.example.userservice.service.EmailService;
import com.example.userservice.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_REQUESTS_PER_MINUTE = 3;
    @Override
    @Transactional
    public OtpResponse sendOtp(SendOtpRequest request) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int recentRequests = otpRepository.countByEmailAndCreatedAtAfter(request.getEmail(), oneMinuteAgo);

        if (recentRequests >= MAX_OTP_REQUESTS_PER_MINUTE) {
            return OtpResponse.builder()
                    .message("Too many OTP requests. Please try again later.")
                    .success(false)
                    .build();
        }

        otpRepository.deleteByEmailAndOtpType(request.getEmail(), request.getOtpType());

        String otpCode = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);

        OtpEntity otpEntity = OtpEntity.builder()
                .email(request.getEmail())
                .otpCode(otpCode)
                .createdAt(now)
                .expiresAt(expiresAt)
                .otpType(request.getOtpType())
                .used(false)
                .build();

        otpRepository.save(otpEntity);

        try {
            emailService.sendOtpEmail(request.getEmail(), otpCode, request.getOtpType());

            return OtpResponse.builder()
                    .message("OTP sent successfully to your email")
                    .success(true)
                    .expiresAt(expiresAt)
                    .build();

        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            return OtpResponse.builder()
                    .message("Failed to send OTP. Please try again.")
                    .success(false)
                    .build();
        }
    }

    @Override
    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request) {
        Optional<OtpEntity> otpEntityOpt = otpRepository.findByEmailAndOtpCodeAndUsedAndExpiresAtAfter(
                request.getEmail(), request.getOtpCode(),false, LocalDateTime.now());
        if (otpEntityOpt.isPresent()) {
            OtpEntity otpEntity = otpEntityOpt.get();
            otpEntity.setUsed(true);
            otpRepository.save(otpEntity);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteAll(
                otpRepository.findAll()
                        .stream()
                        .filter(otp -> otp.getExpiresAt().isBefore(LocalDateTime.now()))
                        .toList()
        );
    }
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
}
