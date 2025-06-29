package com.example.userservice.model.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private String fullName;
    private String email;
    private String phone;
}
