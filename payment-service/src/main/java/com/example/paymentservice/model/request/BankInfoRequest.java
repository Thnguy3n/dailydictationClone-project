package com.example.paymentservice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankInfoRequest {
    @NotBlank(message = "Account name is required")
    private String accountName;
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    @NotBlank(message = "AcqId is required")
    private String acqId;
    private String template;
}
