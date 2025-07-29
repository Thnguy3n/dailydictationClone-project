package com.example.userservice.model.request;

import com.example.userservice.utils.RegisterValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterValidation
public class UserRequest {
    @NotBlank(message = "Username is required") @Size(max=15,min=6,message = "Username must be between 6 and 15 characters")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Email is required") @Email(message = "Email is invalid")
    private String email;
    @NotBlank(message = "Phone number is required")
    private String phone;
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP code must be 6 digits")
    private String otpCode;
}
