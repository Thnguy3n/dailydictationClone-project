package com.example.userservice.controller;
import com.example.userservice.model.request.ChangePasswordRequest;
import com.example.userservice.model.request.UserRequest;
import com.example.userservice.model.response.ChangePasswordResponse;
import com.example.userservice.model.response.ProfileResponse;
import com.example.userservice.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Value("${jwt-secret}")
    private String secretKey;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = header.substring(7);
        return userService.getProfile(getUsernameFromToken(token));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UserRequest userRequest) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = header.substring(7);
        userRequest.setUsername(getUsernameFromToken(token));
        return userService.updateProfile(userRequest);
    }
    @PostMapping("/update-password")
    public ResponseEntity<ChangePasswordResponse> updatePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest changePasswordRequest) {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = header.substring(7);
        changePasswordRequest.setUsername(getUsernameFromToken(token));
        return userService.updatePassword(changePasswordRequest);
    }
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
