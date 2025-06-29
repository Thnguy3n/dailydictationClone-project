package com.example.userservice.service;

import com.example.userservice.model.request.ChangePasswordRequest;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.AuthResponse;
import com.example.userservice.model.response.ChangePasswordResponse;
import com.example.userservice.model.response.ProfileResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<AuthResponse> validateCredentials(UserRequest userRequest);

    ResponseEntity<ProfileResponse> getProfile(String username);

    ResponseEntity<ProfileResponse> updateProfile(UserRequest userRequest);

    ResponseEntity<ChangePasswordResponse> updatePassword(ChangePasswordRequest changePasswordRequest);
}
