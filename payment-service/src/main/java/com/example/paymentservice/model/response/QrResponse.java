package com.example.paymentservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class QrResponse {
    private Long id;
    private String code;
    private String description;
    private LocalDateTime expireAt;
    private com.example.paymentservice.model.response.Data data;

}
