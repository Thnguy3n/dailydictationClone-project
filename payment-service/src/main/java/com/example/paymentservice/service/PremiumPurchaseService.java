package com.example.paymentservice.service;

import com.example.paymentservice.model.request.PremiumPurchaseRequest;
import org.springframework.http.ResponseEntity;

public interface PremiumPurchaseService {
    ResponseEntity<String> addPremiumPurchase(PremiumPurchaseRequest premiumPurchaseRequest, String username);
}
