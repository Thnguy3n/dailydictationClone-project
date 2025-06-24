//package com.example.apigateway.service;
//
//import com.example.apigateway.model.request.UserRequest;
//import com.example.apigateway.model.response.AuthResponse;
//import com.example.apigateway.model.response.MyUserDetailResponse;
//import com.example.apigateway.model.response.UserResponse;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@FeignClient(name = "user-service")
//public interface UserServiceClient {
//     @PostMapping("/api/users/auth/register")
//     ResponseEntity<UserResponse> registerUser(UserRequest userRequest);
//     @GetMapping("/api/users/auth/user-details/{username}")
//     ResponseEntity<MyUserDetailResponse> getUserDetails(@PathVariable String username) ;
//}
