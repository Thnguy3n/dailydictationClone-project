package com.example.paymentservice.controller;

import com.example.paymentservice.model.request.QrRequest;
import com.example.paymentservice.model.response.PaymentStatusResponse;
import com.example.paymentservice.model.response.QrResponse;
import com.example.paymentservice.service.QRTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/qr")
@RequiredArgsConstructor
public class QrController {
    private final QRTransactionService qrTransactionService;
    @PostMapping("/generate")
    public ResponseEntity<QrResponse> generateQrCode(@RequestBody QrRequest qrRequest) {
        return qrTransactionService.generateQrCode(qrRequest);
    }
    @GetMapping("/status/{qrTransactionId}")
    public ResponseEntity<PaymentStatusResponse> checkStatus(@PathVariable Long qrTransactionId) {
        return qrTransactionService.checkPaymentStatus(qrTransactionId);
    }
    @PostMapping("/monitor/{qrTransactionId}")
    public ResponseEntity<PaymentStatusResponse> startMonitoring(@PathVariable Long qrTransactionId) {
        return qrTransactionService.startPaymentMonitoring(qrTransactionId);
    }
}
