package com.example.userservice.controller;

import com.example.userservice.model.request.OAuth2LoginRequest;
import com.example.userservice.model.response.AuthResponse;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.OAuthResponse;
import com.example.userservice.model.response.UserResponse;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest)  {
        return authService.registerUser(userRequest);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserRequest userRequest) {
        return userService.validateCredentials(userRequest);
    }
    @PostMapping("/oauth2-login")
    public ResponseEntity<OAuthResponse> oauth2Login(@Valid @RequestBody OAuth2LoginRequest loginRequest) {
        return authService.registerOrUpdateOAuth2UserAndGenerateToken(loginRequest);
    }



}
