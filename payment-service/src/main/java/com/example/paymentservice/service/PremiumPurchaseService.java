package com.example.paymentservice.service;

import com.example.paymentservice.model.request.PremiumPurchaseRequest;
import com.example.paymentservice.model.response.PurchaseResponse;
import org.springframework.http.ResponseEntity;

public interface PremiumPurchaseService {
    ResponseEntity<PurchaseResponse> addPremiumPurchase(PremiumPurchaseRequest premiumPurchaseRequest, String username);
}
