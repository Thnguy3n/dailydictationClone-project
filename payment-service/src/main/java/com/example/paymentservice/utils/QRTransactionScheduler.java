package com.example.paymentservice.utils;

import com.example.paymentservice.entity.QrTransactionEntity;
import com.example.paymentservice.repository.PremiumPurchaseRepository;
import com.example.paymentservice.repository.QRTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QRTransactionScheduler {
    private final QRTransactionRepository qrTransactionRepository;
    private final PremiumPurchaseRepository premiumPurchaseRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void cancelExpiredTransactions() {
        LocalDateTime now = LocalDateTime.now();
        List<QrTransactionEntity> expiredTransactions = qrTransactionRepository
                .findByExpiresAtBeforeAndPurchase_Status(now, "PENDING");

        for (QrTransactionEntity transaction : expiredTransactions) {
            transaction.getPurchase().setStatus("CANCELLED");
            premiumPurchaseRepository.save(transaction.getPurchase());
            log.info("Auto-cancelled expired QR transaction: {}", transaction.getId());
        }
    }
}