package com.example.userservice.service;

import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.UserResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<UserResponse> registerUser(UserRequest userRequest);

}
