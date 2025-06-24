//package com.example.apigateway.service;
//
//import com.example.apigateway.model.UserPrincipal;
//import com.example.apigateway.model.response.MyUserDetailResponse;
//import lombok.RequiredArgsConstructor;
//import com.example.apigateway.model.response.AuthResponse;
//import com.example.apigateway.model.request.UserRequest;
//import com.example.apigateway.model.response.UserResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//    private final UserServiceClient userServiceClient;
//    private final JwtService jwtService;
//    private final PasswordEncoder passwordEncoder;
//
//    public Mono<ResponseEntity<UserResponse>> register(UserRequest request) {
//        Mono<ResponseEntity<UserResponse>> response = Mono.fromCallable(() -> userServiceClient.registerUser(request))
//                .subscribeOn(Schedulers.boundedElastic())
//                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserResponse(null,null,null, "Registration failed")));
//
//        return response;
//    }
//
//    public Mono<ResponseEntity<AuthResponse>> login(UserRequest userRequest) {
//        return getUserDetails(userRequest.getUsername())
//                .map(ResponseEntity::getBody)
//                .filter(userDetails -> userDetails != null)
//                .filter(userDetails -> passwordEncoder.matches(userRequest.getPassword(), userDetails.getPassword()))
//                .map(userDetails -> {
//                    String token = jwtService.generateToken(userRequest.getUsername());
//                    return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
//                })
//                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new AuthResponse(null, "Invalid credentials"))))
//                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(new AuthResponse(null, "Authentication failed")));
//    }
//
//    public Mono<ResponseEntity<UserDetails>> getUserDetails(String username) {
//        ResponseEntity<MyUserDetailResponse> responseEntity = userServiceClient.getUserDetails(username);
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            UserDetails userDetails = new UserPrincipal(responseEntity.getBody());
//            return Mono.just(new ResponseEntity<>(userDetails, HttpStatus.OK));
//        }
//        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(null));
//    }
//}