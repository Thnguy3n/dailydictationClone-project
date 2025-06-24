package com.example.userservice.model;

import lombok.Data;

@Data
public class MyUserDetailResponse {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String role;
    private String email;
    private Integer isActive;

}
