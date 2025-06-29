package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.UserResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
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

    @Override
    public ResponseEntity<UserResponse> registerUser(UserRequest userRequest) {
        if (userRepository.existsByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail())) {
            return new ResponseEntity<>(UserResponse.builder()
                    .message("Username or Email already exists")
                    .build(), HttpStatus.BAD_REQUEST);
        }
        UserEntity userEntity = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .phone(userRequest.getPhone())
                .email(userRequest.getEmail())
                .fullName(userRequest.getFullName())
                .isActive(1)
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

}
