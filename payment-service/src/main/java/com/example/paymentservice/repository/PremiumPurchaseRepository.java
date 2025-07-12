package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PremiumPurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumPurchaseRepository extends JpaRepository<PremiumPurchaseEntity, String> {
}
