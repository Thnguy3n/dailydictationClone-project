package com.example.paymentservice.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseResponse {
    private String purchaseId;
}
