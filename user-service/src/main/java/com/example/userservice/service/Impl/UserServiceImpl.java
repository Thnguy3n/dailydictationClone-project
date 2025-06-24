package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.AuthResponse;
import com.example.userservice.model.UserRequest;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.JwtService;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<AuthResponse> validateCredentials(UserRequest userRequest) {
        UserEntity user = userRepository.findByUsername(userRequest.getUsername());
        if (user == null) {
            return new ResponseEntity<>(new AuthResponse(null,"Invalid username"), HttpStatus.UNAUTHORIZED);
        }
        if (!isPasswordValid(user, userRequest)) {
            return new ResponseEntity<>(new AuthResponse(null,"Invalid password"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new AuthResponse(jwtService.generateToken(userRequest.getUsername()), "Login successful"), HttpStatus.OK);
    }

    private boolean isPasswordValid(UserEntity user, UserRequest userRequest) {
        return passwordEncoder.matches(userRequest.getPassword(), user.getPassword());
    }
}
