package com.example.paymentservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionData {
    private String transactionId;
    private String description;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
}
