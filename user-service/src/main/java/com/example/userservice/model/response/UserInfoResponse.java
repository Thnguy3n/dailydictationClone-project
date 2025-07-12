package com.example.userservice.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserInfoResponse {
    private String userId;
}
