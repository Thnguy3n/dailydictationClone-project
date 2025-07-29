package com.example.userservice.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Email is required") @Email(message = "Email is invalid")
    private String email;
    @NotBlank(message = "Phone number is required")
    private String phone;
}
