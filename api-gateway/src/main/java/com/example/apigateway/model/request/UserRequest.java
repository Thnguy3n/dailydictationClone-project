//package com.example.apigateway.model.request;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import com.example.apigateway.utils.RegisterValidation;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@RegisterValidation
//public class UserRequest {
//    @NotBlank(message = "Username is required") @Size(max=15,min=6,message = "Username must be between 6 and 15 characters")
//    private String username;
//    @NotBlank(message = "Password is required")
//    private String password;
//    @NotBlank(message = "Full name is required")
//    private String fullName;
//    @NotBlank(message = "Email is required") @Email(message = "Email is invalid")
//    private String email;
//    @NotBlank(message = "Phone number is required")
//    private String phone;
//}
