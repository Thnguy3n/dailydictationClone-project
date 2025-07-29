package com.example.userservice.service.Impl;

import com.example.userservice.constants.OtpType;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.request.OAuth2LoginRequest;
import com.example.userservice.model.request.SendOtpRequest;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.request.VerifyOtpRequest;
import com.example.userservice.model.response.OAuthResponse;
import com.example.userservice.model.response.OtpResponse;
import com.example.userservice.model.response.UserResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.JwtService;
import com.example.userservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder =  new BCryptPasswordEncoder(12);
    private final JwtService jwtService;
    private final OtpService otpService;

    @Override
    public ResponseEntity<UserResponse> registerUser(UserRequest userRequest) {
        if (userRepository.existsByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail())) {
            return new ResponseEntity<>(UserResponse.builder()
                    .message("Username or Email already exists")
                    .build(), HttpStatus.BAD_REQUEST);
        }

        VerifyOtpRequest otpRequest = VerifyOtpRequest.builder()
                .email(userRequest.getEmail())
                .otpCode(userRequest.getOtpCode())
                .otpType(OtpType.EMAIL_VERIFICATION)
                .build();

        if (otpService.verifyOtp(otpRequest)) {
            return new ResponseEntity<>(UserResponse.builder()
                    .message("Invalid or expired OTP code")
                    .build(), HttpStatus.BAD_REQUEST);
        }

        UserEntity userEntity = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .phone(userRequest.getPhone())
                .email(userRequest.getEmail())
                .fullName(userRequest.getFullName())
                .isActive(1)
                .premiumStatus(0)
                .role("USER")
                .createAt(LocalDateTime.now())
                .modifiedAt(null)
                .build();
        userRepository.save(userEntity);
        UserResponse userResponse = UserResponse.builder()
                .fullName(userEntity.getFullName())
                .message("User registered successfully")
                .build();
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<OAuthResponse> registerOrUpdateOAuth2UserAndGenerateToken(OAuth2LoginRequest loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            UserEntity newUser = UserEntity.builder()
                    .username(loginRequest.getEmail())
                    .email(loginRequest.getEmail())
                    .fullName(loginRequest.getDisplayName())
                    .isActive(1)
                    .role("USER")
                    .premiumStatus(0)
                    .createAt(LocalDateTime.now())
                    .modifiedAt(null)
                    .build();
            userRepository.save(newUser);
            return new ResponseEntity<>(OAuthResponse.builder()
                    .message("User registered successfully")
                    .token(jwtService.generateToken(newUser.getUsername()))
                    .build(), HttpStatus.CREATED);
        } else {
            user.setModifiedAt(LocalDateTime.now());
            userRepository.save(user);
            return new ResponseEntity<>(OAuthResponse.builder()
                    .message("User updated successfully")
                    .token(jwtService.generateToken(user.getUsername()))
                    .build(), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<OtpResponse> sendOtpForEmailVerification(SendOtpRequest request) {
        // Check if email already exists for EMAIL_VERIFICATION type
        if (request.getOtpType() == OtpType.EMAIL_VERIFICATION &&
                userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(OtpResponse.builder()
                    .message("Email already registered")
                    .success(false)
                    .build(), HttpStatus.BAD_REQUEST);
        }

        OtpResponse response = otpService.sendOtp(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }

    @Override
    public ResponseEntity<OtpResponse> verifyEmailOtp(VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request);

        OtpResponse response = OtpResponse.builder()
                .message(isValid ? "OTP verified successfully" : "Invalid or expired OTP")
                .success(isValid)
                .build();

        HttpStatus status = isValid ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }

}
