package com.example.userservice.service.Impl;

import com.example.userservice.constants.OtpType;
import com.example.userservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Override
    public void sendOtpEmail(String toEmail, String otpCode, OtpType otpType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);

                String subject = getEmailSubject(otpType);
                String content = buildEmailContent(otpCode, otpType);

                helper.setSubject(subject);
                helper.setText(content, false);

                mailSender.send(message);
                log.info("OTP email sent successfully to: {}", toEmail);

            } catch (MessagingException e) {
                log.error("Failed to set email properties for: {}", toEmail, e);
                throw new RuntimeException("Failed to configure email message", e);
            }

        } catch (MessagingException e) {
            log.error("Failed to create MimeMessage for: {}", toEmail, e);
            throw new RuntimeException("Failed to create email message", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    private String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Email Verification - OTP Code";
            case PASSWORD_RESET -> "Password Reset - OTP Code";
        };
    }

    private String buildEmailContent(String otpCode, OtpType otpType) {
        String purpose = switch (otpType) {
            case EMAIL_VERIFICATION -> "verify your email address";
            case PASSWORD_RESET -> "reset your password";
        };

        return String.format("Your OTP code to %s is: %s. This code will expire in 5 minutes.",
                purpose, otpCode);
    }
}
