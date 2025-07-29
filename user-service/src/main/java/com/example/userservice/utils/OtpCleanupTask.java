package com.example.userservice.utils;

import com.example.userservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupTask {
    private final OtpService otpService;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredOtps() {
        log.info("Starting OTP cleanup task");
        otpService.cleanupExpiredOtps();
        log.info("Completed OTP cleanup task");
    }
}