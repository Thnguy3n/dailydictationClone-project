package com.example.paymentservice.service;

import com.example.paymentservice.model.request.PackageRequest;
import com.example.paymentservice.model.response.PremiumPackageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PremiumPackageService {
    ResponseEntity<String> addPremiumPackage(PackageRequest packageRequest);

    PremiumPackageResponse getPremiumPackage(Long id);

    List<PremiumPackageResponse> getAllPremiumPackages();

    ResponseEntity<String> updatePremiumPackage(Long id,PackageRequest packageRequest);

    ResponseEntity<String> deletePremiumPackage(Long id);

}
