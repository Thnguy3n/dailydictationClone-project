package com.example.paymentservice.service.Impl;

import com.example.paymentservice.entity.PremiumPackageEntity;
import com.example.paymentservice.entity.PremiumPurchaseEntity;
import com.example.paymentservice.model.request.PremiumPurchaseRequest;
import com.example.paymentservice.model.response.PurchaseResponse;
import com.example.paymentservice.model.response.UserInfoResponse;
import com.example.paymentservice.repository.PremiumPackageRepository;
import com.example.paymentservice.repository.PremiumPurchaseRepository;
import com.example.paymentservice.service.PremiumPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PremiumPurchaseServiceImpl implements PremiumPurchaseService {
    private final RestTemplate restTemplate;
    private final PremiumPurchaseRepository premiumPurchaseRepository;
    private final PremiumPackageRepository premiumPackageRepository;

    @Override
    public ResponseEntity<PurchaseResponse> addPremiumPurchase(PremiumPurchaseRequest premiumPurchaseRequest, String username) {
        PremiumPackageEntity premiumPackageEntity = premiumPackageRepository.findById(premiumPurchaseRequest.getPremiumPackageId())
                .orElseThrow(() -> new IllegalArgumentException("Premium package not found"));
        PremiumPurchaseEntity premiumPurchaseEntity = PremiumPurchaseEntity.builder()
                .userId(Objects.requireNonNull(getUserInfo(username).getBody()).getUserId())
                .premiumPackage(premiumPackageEntity)
                .price(premiumPackageEntity.getPrice())
                .status("PENDING")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(premiumPackageEntity.getDurationInMonths()))
                .build();
        premiumPurchaseRepository.save(premiumPurchaseEntity);
        PurchaseResponse purchaseResponse = PurchaseResponse.builder()
                .purchaseId(premiumPurchaseEntity.getId())
                .build();
        return ResponseEntity.ok(purchaseResponse);
    }
    private ResponseEntity<UserInfoResponse> getUserInfo(String username) {
        String url = "https://user-service/api/users/info/" + username;
        ResponseEntity<UserInfoResponse> response = restTemplate.getForEntity(url, UserInfoResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
