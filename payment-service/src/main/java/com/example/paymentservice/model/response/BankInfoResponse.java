package com.example.paymentservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankInfoResponse {
    private String accountName;
    private String accountNumber;
    private String acqId;
    private String template;
}
