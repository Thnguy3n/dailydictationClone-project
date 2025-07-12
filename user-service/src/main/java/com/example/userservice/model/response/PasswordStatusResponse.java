package com.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordStatusResponse {
    private boolean hasPassword;
}
