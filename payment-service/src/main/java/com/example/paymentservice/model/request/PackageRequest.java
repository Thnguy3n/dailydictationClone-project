package com.example.paymentservice.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PackageRequest {
    @NotBlank
    private String name;
    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;
    private Integer discount = 0;
    private LocalDateTime discountStart;
    private LocalDateTime discountEnd;
    private String description;
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationInMonths;
}
