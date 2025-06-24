package com.example.apigateway.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RegistrationValidator.class)
public @interface RegisterValidation {
    String message() default "Register information is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
