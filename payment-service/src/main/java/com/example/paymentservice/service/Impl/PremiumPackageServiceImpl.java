package com.example.paymentservice.service.Impl;

import com.example.paymentservice.entity.PremiumPackageEntity;
import com.example.paymentservice.model.request.PackageRequest;
import com.example.paymentservice.model.response.PremiumPackageResponse;
import com.example.paymentservice.repository.PremiumPackageRepository;
import com.example.paymentservice.service.PremiumPackageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PremiumPackageServiceImpl implements PremiumPackageService {
    private final PremiumPackageRepository premiumPackageRepository;
    @Override
    public ResponseEntity<String> addPremiumPackage(PackageRequest packageRequest) {
        if (!packageRequest.getDiscountStart().isBefore(packageRequest.getDiscountEnd())) {
            return ResponseEntity.badRequest().body("Discount start date must be before discount end date.");
        }
        if (packageRequest.getDiscount() < 0 || packageRequest.getDiscount() > 100) {
            return ResponseEntity.badRequest()
                    .body("Discount must be between 0 and 100");
        }

        PremiumPackageEntity premiumPackageEntity = PremiumPackageEntity.builder()
                .name(packageRequest.getName())
                .originalPrice(packageRequest.getPrice())
                .price(packageRequest.getPrice())
                .discount(packageRequest.getDiscount())
                .discountStart(packageRequest.getDiscountStart())
                .discountEnd(packageRequest.getDiscountEnd())
                .description(packageRequest.getDescription())
                .durationInMonths(packageRequest.getDurationInMonths())
                .build();

        premiumPackageRepository.save(premiumPackageEntity);
        return ResponseEntity.status(201).body("Premium package added successfully.");
    }


    @Override
    public PremiumPackageResponse getPremiumPackage(Long id) {
        PremiumPackageEntity entity = premiumPackageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found"));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        boolean isDiscountActive = now.isAfter(entity.getDiscountStart()) &&
                now.isBefore(entity.getDiscountEnd());
        PremiumPackageResponse response = PremiumPackageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .originalPrice(entity.getOriginalPrice())
                .price(isDiscountActive ?
                        calculateDiscountedPrice(entity.getOriginalPrice(), entity.getDiscount()) :
                        entity.getOriginalPrice())
                .discount(entity.getDiscount())
                .discountStatus(isDiscountActive ? "ON" : "OFF")
                .discountStart(entity.getDiscountStart())
                .discountEnd(entity.getDiscountEnd())
                .description(entity.getDescription())
                .build();
        entity.setPrice(response.getPrice());
        entity.setDiscountStatus(response.getDiscountStatus());
        premiumPackageRepository.save(entity);
        return response;

    }
    @Override
    public List<PremiumPackageResponse> getAllPremiumPackages() {
        List<PremiumPackageEntity> entities = premiumPackageRepository.findAll();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<PremiumPackageEntity> updatedEntities = entities.stream()
                .map(entity -> {
                    boolean isDiscountActive = now.isAfter(entity.getDiscountStart()) &&
                            now.isBefore(entity.getDiscountEnd());

                    BigDecimal price = isDiscountActive ?
                            calculateDiscountedPrice(entity.getOriginalPrice(), entity.getDiscount()) :
                            entity.getOriginalPrice();
                    String discountStatus = isDiscountActive ? "ON" : "OFF";

                    entity.setPrice(price);
                    entity.setDiscountStatus(discountStatus);
                    return entity;
                })
                .collect(Collectors.toList());

        premiumPackageRepository.saveAll(updatedEntities);

        return updatedEntities.stream()
                .map(entity -> PremiumPackageResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .originalPrice(entity.getOriginalPrice())
                        .price(entity.getPrice())
                        .discount(entity.getDiscount())
                        .discountStatus(entity.getDiscountStatus())
                        .discountStart(entity.getDiscountStart())
                        .discountEnd(entity.getDiscountEnd())
                        .description(entity.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<String> updatePremiumPackage(Long id, PackageRequest packageRequest) {
        try {
            PremiumPackageEntity entity = premiumPackageRepository.findById(id)
                    .orElse(null);

            if (entity == null) {
                return ResponseEntity.notFound().build();
            }

            if (!packageRequest.getDiscountStart().isBefore(packageRequest.getDiscountEnd())) {
                return ResponseEntity.badRequest().body("Discount start date must be before discount end date.");
            }
            if (packageRequest.getDiscount() < 0 || packageRequest.getDiscount() > 100) {
                return ResponseEntity.badRequest()
                        .body("Discount must be between 0 and 100");
            }

            entity.setName(packageRequest.getName());
            entity.setOriginalPrice(packageRequest.getPrice());
            entity.setDiscount(packageRequest.getDiscount());
            entity.setDiscountStart(packageRequest.getDiscountStart());
            entity.setDiscountEnd(packageRequest.getDiscountEnd());
            entity.setDescription(packageRequest.getDescription());

            premiumPackageRepository.save(entity);
            return ResponseEntity.ok().body("Successfully updated premium package.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating premium package: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> deletePremiumPackage(Long id) {
        try {
            if (!premiumPackageRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            premiumPackageRepository.deleteById(id);
            return ResponseEntity.ok().body("Successfully deleted premium package.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting premium package: " + e.getMessage());
        }
    }


    public BigDecimal calculateDiscountedPrice(BigDecimal price, Integer discount) {
        return price
                .multiply(BigDecimal.valueOf(100 - discount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
