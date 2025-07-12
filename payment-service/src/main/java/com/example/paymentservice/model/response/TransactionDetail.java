package com.example.paymentservice.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetail {
    @JsonProperty("Mã GD")
    private Long transactionId;

    @JsonProperty("Mô tả")
    private String description;

    @JsonProperty("Giá trị")
    private BigDecimal amount;

    @JsonProperty("Ngày diễn ra")
    private String transactionTime;

    @JsonProperty("Số tài khoản")
    private String accountNumber;
}
