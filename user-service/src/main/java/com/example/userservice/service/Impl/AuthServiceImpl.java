package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.UserRequest;
import com.example.userservice.model.UserResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder =  new BCryptPasswordEncoder(12);

    @Override
    public ResponseEntity<UserResponse> registerUser(UserRequest userRequest) {
        UserEntity userEntity = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .phone(userRequest.getPhone())
                .fullName(userRequest.getFullName())
                .isActive(1)
                .role("USER")
                .build();
        userRepository.save(userEntity);
        return new ResponseEntity<>(modelMapper.map(userEntity,UserResponse.class), HttpStatus.CREATED);
    }

}
