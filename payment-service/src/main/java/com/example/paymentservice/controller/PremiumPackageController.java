package com.example.paymentservice.controller;

import com.example.paymentservice.model.request.PackageRequest;
import com.example.paymentservice.model.response.PremiumPackageResponse;
import com.example.paymentservice.service.PremiumPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment/premium-package")
@RequiredArgsConstructor
public class PremiumPackageController {
    private final PremiumPackageService premiumPackageService;
    @PostMapping()
    public ResponseEntity<String> addPremiumPackage(@Valid @RequestBody PackageRequest packageRequest) {
        return premiumPackageService.addPremiumPackage(packageRequest);
    }
    @GetMapping("/info/{id}")
    public ResponseEntity<PremiumPackageResponse> getPremiumPackage(@PathVariable Long id) {
        return ResponseEntity.ok(premiumPackageService.getPremiumPackage(id));
    }
    @GetMapping("/all")
    public ResponseEntity<List<PremiumPackageResponse>> getAllPremiumPackages() {
        return ResponseEntity.ok(premiumPackageService.getAllPremiumPackages());
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePremiumPackage(@PathVariable Long id,
                                                       @Valid @RequestBody PackageRequest packageRequest) {
        return premiumPackageService.updatePremiumPackage(id, packageRequest);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePremiumPackage(@PathVariable Long id) {
        return premiumPackageService.deletePremiumPackage(id);
    }
}
