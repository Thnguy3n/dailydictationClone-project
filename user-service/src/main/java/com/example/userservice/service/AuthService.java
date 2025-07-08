package com.example.userservice.service;

import com.example.userservice.model.request.OAuth2LoginRequest;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.AuthResponse;
import com.example.userservice.model.response.OAuthResponse;
import com.example.userservice.model.response.UserResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<UserResponse> registerUser(UserRequest userRequest);

    ResponseEntity<OAuthResponse> registerOrUpdateOAuth2UserAndGenerateToken(OAuth2LoginRequest loginRequest);

}
