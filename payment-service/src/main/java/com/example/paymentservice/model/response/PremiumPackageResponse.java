package com.example.paymentservice.model.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PremiumPackageResponse {
    private Long id;
    private String name;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer discount;
    private LocalDateTime discountStart;
    private LocalDateTime discountEnd;
    private String discountStatus;
    private String description;
}
