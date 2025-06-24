package com.example.userservice.service;

import com.example.userservice.model.UserRequest;
import com.example.userservice.model.UserResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<UserResponse> registerUser(UserRequest userRequest);

}
