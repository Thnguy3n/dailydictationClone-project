package com.example.paymentservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QrRequest {
    private String purchaseId;
    private Long bankInfoId;
}
