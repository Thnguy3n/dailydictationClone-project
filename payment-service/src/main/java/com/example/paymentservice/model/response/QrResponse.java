package com.example.paymentservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class QrResponse {
    private String code;
    private String description;
    private com.example.paymentservice.model.response.Data data;

}
