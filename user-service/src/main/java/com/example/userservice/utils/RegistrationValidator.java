package com.example.userservice.utils;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.model.UserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

public class RegistrationValidator implements ConstraintValidator<RegisterValidation, UserRequest> {

    @Override
    public void initialize(RegisterValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(UserRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if (!request.getUsername().matches("^[a-zA-Z0-9_.-]+$")){
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Username can only contain letters, numbers, and the characters _ . -");
        }
        if(!request.getPassword().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).*$")){
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }
        if(!request.getFullName().matches("^\\p{L}+(?: \\p{L}+)*$")){
            throw new ValidationException("Full name is invalid.");
        }
        if(!request.getPhone().matches("^(0|\\+84)\\d{9,10}$")){
            constraintValidatorContext.disableDefaultConstraintViolation();
            throw new ValidationException("Phone number must be start with +84 or 0...");
        }
        return true;
    }
}
