package com.example.userservice.service.Impl;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.request.ChangePasswordRequest;
import com.example.userservice.model.request.UpdateProfileRequest;
import com.example.userservice.model.response.AuthResponse;
import com.example.userservice.model.response.ChangePasswordResponse;
import com.example.userservice.model.response.ProfileResponse;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.JwtService;
import com.example.userservice.service.UserService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;
    @Override
    public ResponseEntity<AuthResponse> validateCredentials(UserRequest userRequest) {
        UserEntity user = userRepository.findByUsername(userRequest.getUsername());
        if (user == null) {
            return new ResponseEntity<>(new AuthResponse(null,"Invalid username"), HttpStatus.UNAUTHORIZED);
        }
        if (!isPasswordValid(user, userRequest.getPassword())) {
            return new ResponseEntity<>(new AuthResponse(null,"Invalid password"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new AuthResponse(jwtService.generateToken(userRequest.getUsername()), "Login successful"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProfileResponse> getProfile(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(modelMapper.map(user, ProfileResponse.class), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProfileResponse> updateProfile(UpdateProfileRequest userRequest) {
        if(!userRequest.getPhone().matches("^(0|\\+84)\\d{9,10}$")){
            throw new ValidationException("Phone number must be start with +84 or 0...");
        }
        UserEntity user = userRepository.findByUsername(userRequest.getUsername());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setFullName(userRequest.getFullName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        userRepository.save(user);
        return new ResponseEntity<>(modelMapper.map(user, ProfileResponse.class), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChangePasswordResponse> updatePassword(ChangePasswordRequest changePasswordRequest) {
        UserEntity user = userRepository.findByUsername(changePasswordRequest.getUsername());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!isPasswordValid(user, changePasswordRequest.getOldPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponse("Invalid old password"), HttpStatus.BAD_REQUEST);
        }
        if(!changePasswordRequest.getNewPassword().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).*$")){
            throw new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
        return new ResponseEntity<>(new ChangePasswordResponse("Password updated successfully"), HttpStatus.OK);

    }

    private boolean isPasswordValid(UserEntity user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
