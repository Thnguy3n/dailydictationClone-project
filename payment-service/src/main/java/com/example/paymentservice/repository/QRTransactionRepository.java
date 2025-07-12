package com.example.paymentservice.repository;

import com.example.paymentservice.entity.QrTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QRTransactionRepository extends JpaRepository<QrTransactionEntity, Long> {
    List<QrTransactionEntity> findByExpiresAtBeforeAndPurchase_Status(LocalDateTime expiresAtBefore, String purchaseStatus);
}
