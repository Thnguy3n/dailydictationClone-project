package com.example.paymentservice.model.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QrGenerateRequest {
    private String accountNo;
    private String accountName;
    private String acqId;
    private BigDecimal amount;
    private String addInfo;
    private String format;
    private String template;
}
