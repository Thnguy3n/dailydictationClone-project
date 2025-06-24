package com.example.userservice.service;

import com.example.userservice.model.AuthResponse;
import com.example.userservice.model.MyUserDetailResponse;
import com.example.userservice.model.UserRequest;
import com.example.userservice.model.UserResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<AuthResponse> validateCredentials(UserRequest userRequest);

}
