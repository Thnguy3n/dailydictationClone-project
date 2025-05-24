package com.example.userservice.controller;

import com.example.userservice.model.UserRequest;
import com.example.userservice.model.UserResponse;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api")
public class UserController {
    private  final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest)  {
        return userService.registerUser(userRequest);
    }
    @PostMapping("/auth/login")
    public String login(@RequestBody UserRequest userRequest) {
        return userService.login(userRequest);
    }
    @GetMapping("/helloo")
    public String helloo() {
        return "helloo";
    }

}
