package com.example.userservice.service;

import com.example.userservice.constants.OtpType;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode, OtpType otpType);
}
