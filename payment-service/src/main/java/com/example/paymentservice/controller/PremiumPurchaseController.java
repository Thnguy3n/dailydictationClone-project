package com.example.paymentservice.controller;

import com.example.paymentservice.model.request.PremiumPurchaseRequest;
import com.example.paymentservice.service.PremiumPurchaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/premium-purchase")
@RequiredArgsConstructor
public class PremiumPurchaseController {
    @Value("${jwt-secret}")
    private String secretKey;
    private final PremiumPurchaseService premiumPurchaseService;
    @PostMapping
    public ResponseEntity<String> purchasePremium(@RequestBody PremiumPurchaseRequest premiumPurchaseRequest,
                                                  HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        String username = getUsernameFromToken(token);
        return premiumPurchaseService.addPremiumPurchase(premiumPurchaseRequest, username);
    }
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
