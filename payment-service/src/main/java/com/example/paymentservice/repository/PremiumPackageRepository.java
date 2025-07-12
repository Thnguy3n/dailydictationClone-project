package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PremiumPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumPackageRepository extends JpaRepository<PremiumPackageEntity, Long> {
}
