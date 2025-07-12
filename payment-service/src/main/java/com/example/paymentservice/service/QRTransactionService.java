package com.example.paymentservice.service;

import com.example.paymentservice.model.request.QrRequest;
import com.example.paymentservice.model.response.PaymentStatusResponse;
import com.example.paymentservice.model.response.QrResponse;
import org.springframework.http.ResponseEntity;

public interface QRTransactionService {
    ResponseEntity<QrResponse> generateQrCode(QrRequest qrRequest);

    ResponseEntity<PaymentStatusResponse> checkPaymentStatus(Long qrTransactionId);

    ResponseEntity<PaymentStatusResponse> startPaymentMonitoring(Long qrTransactionId);
}
