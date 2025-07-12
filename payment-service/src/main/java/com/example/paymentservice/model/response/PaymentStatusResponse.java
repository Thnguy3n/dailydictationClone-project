package com.example.paymentservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusResponse {
    private String status; // "PENDING", "PAID", "EXPIRED", "CANCELLED"
    private String message;
    private LocalDateTime checkedAt;
    private TransactionData transactionData;
}