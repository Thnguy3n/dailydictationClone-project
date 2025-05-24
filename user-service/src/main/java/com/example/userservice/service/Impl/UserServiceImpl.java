package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.UserRequest;
import com.example.userservice.model.UserResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.JwtService;
import com.example.userservice.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder =  new BCryptPasswordEncoder(12);
    private final JwtService jwtService;
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
    }
    @Autowired
    private AuthenticationManager authenticationManager;
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

    @Override
    public String login(UserRequest userRequest) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
        if(authentication.isAuthenticated()) {
            return jwtService.generateToken(userRequest.getUsername());
        }
        return "fail";
    }
}
