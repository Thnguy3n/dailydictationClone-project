package com.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OAuthResponse {
    private String token;
    private String message;
    private String email;
    private String name;
}
