package com.example.userservice.service;

import com.example.userservice.model.request.SendOtpRequest;
import com.example.userservice.model.request.VerifyOtpRequest;
import com.example.userservice.model.response.OtpResponse;

public interface OtpService {
    OtpResponse sendOtp(SendOtpRequest request);
    boolean verifyOtp(VerifyOtpRequest request);
    void cleanupExpiredOtps();
}
