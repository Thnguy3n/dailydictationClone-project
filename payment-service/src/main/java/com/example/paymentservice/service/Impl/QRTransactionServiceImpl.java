package com.example.paymentservice.service.Impl;

import com.example.paymentservice.entity.BankInfoEntity;
import com.example.paymentservice.entity.PremiumPurchaseEntity;
import com.example.paymentservice.entity.QrTransactionEntity;
import com.example.paymentservice.model.request.QrGenerateRequest;
import com.example.paymentservice.model.request.QrRequest;
import com.example.paymentservice.model.response.*;
import com.example.paymentservice.repository.BankInfoRepository;
import com.example.paymentservice.repository.PremiumPurchaseRepository;
import com.example.paymentservice.repository.QRTransactionRepository;
import com.example.paymentservice.service.QRTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRTransactionServiceImpl implements QRTransactionService {
    private final PremiumPurchaseRepository premiumPurchaseRepository;
    private final RestTemplate restTemplate;
    private final BankInfoRepository bankInfoRepository;
    private final QRTransactionRepository qrTransactionRepository;
    @Value("${BANK_SCRIPT_URL}")
    private String BANK_SCRIPT_URL;
    @Autowired
    @Qualifier("externalRestTemplate")
    private RestTemplate externalRestTemplate;
    @Override
    public ResponseEntity<QrResponse> generateQrCode(QrRequest qrRequest) {
        PremiumPurchaseEntity premiumPurchase = premiumPurchaseRepository.findById(qrRequest.getPurchaseId())
                .orElseThrow(() -> new RuntimeException("Premium purchase not found"));
        BankInfoEntity bankInfo = bankInfoRepository.findAll().getFirst();

        if (!"PENDING".equals(premiumPurchase.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        QrTransactionEntity qrTransactionEntity =QrTransactionEntity.builder()
                .purchase(premiumPurchase)
                .bankInfo(bankInfo)
                .addInfo("Payment for " + premiumPurchase.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();

        QrGenerateRequest qrGenerateRequest = QrGenerateRequest.builder()
                .accountNo(bankInfo.getAccountNumber())
                .accountName(bankInfo.getAccountName())
                .acqId(bankInfo.getAcqId())
                .amount(premiumPurchase.getPrice())
                .addInfo(qrTransactionEntity.getAddInfo())
                .format(bankInfo.getFormat())
                .template(bankInfo.getTemplate())
                .build();
        QrResponse qrResponse = vietQrIoGenerate(qrGenerateRequest).getBody();
        qrTransactionEntity.setQrDataUrl(qrResponse.getData().getQrDataURL());
        qrTransactionRepository.save(qrTransactionEntity);
        qrResponse.setId(qrTransactionEntity.getId());
        qrResponse.setAddInfo(qrTransactionEntity.getAddInfo());
        qrResponse.setExpireAt(qrTransactionEntity.getExpiresAt());
        return ResponseEntity.ok(qrResponse);
    }
    private ResponseEntity<QrResponse> vietQrIoGenerate(QrGenerateRequest qrRequest) {
        String url = "https://api.vietqr.io/v2/generate";
        ResponseEntity<QrResponse> response = externalRestTemplate.postForEntity(url, qrRequest, QrResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    @Override
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(Long qrTransactionId) {
        QrTransactionEntity qrTransaction = qrTransactionRepository.findById(qrTransactionId)
                .orElseThrow(() -> new RuntimeException("QR Transaction not found"));

        if (isQrExpired(qrTransaction)) {
            cancelExpiredTransaction(qrTransaction);
            return ResponseEntity.ok(new PaymentStatusResponse(
                    "EXPIRED",
                    "QR code has expired",
                    LocalDateTime.now()
            ));
        }

        PaymentStatusResponse paymentStatus = checkBankTransaction(qrTransaction);

        if ("PAID".equals(paymentStatus.getStatus())) {
            updatePurchaseStatus(qrTransaction.getPurchase(), "PAID");
        }

        return ResponseEntity.ok(paymentStatus);
    }
    @Override
    public ResponseEntity<PaymentStatusResponse> startPaymentMonitoring(Long qrTransactionId) {
        QrTransactionEntity qrTransaction = qrTransactionRepository.findById(qrTransactionId)
                .orElseThrow(() -> new RuntimeException("QR Transaction not found"));

        return monitorPaymentStatus(qrTransaction);
    }
    private boolean isQrExpired(QrTransactionEntity qrTransaction) {
        return LocalDateTime.now().isAfter(qrTransaction.getExpiresAt());
    }

    private void cancelExpiredTransaction(QrTransactionEntity qrTransaction) {
        updatePurchaseStatus(qrTransaction.getPurchase(), "CANCELLED");
        log.info("Cancelled expired QR transaction: {}", qrTransaction.getId());
    }

    private PaymentStatusResponse checkBankTransaction(QrTransactionEntity qrTransaction) {
        try {
            ResponseEntity<BankTransactionResponse> response = externalRestTemplate.getForEntity(
                    BANK_SCRIPT_URL, BankTransactionResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BankTransactionResponse bankResponse = response.getBody();

                if (!bankResponse.isError() && bankResponse.getData() != null && !bankResponse.getData().isEmpty()) {
                    TransactionDetail latestTransaction = bankResponse.getData().get(0);

                    if (isTransactionMatched(latestTransaction, qrTransaction)) {
                        return new PaymentStatusResponse(
                                "PAID",
                                "Payment confirmed",
                                LocalDateTime.now()

                        );
                    }
                }
            }

            return new PaymentStatusResponse(
                    "PENDING",
                    "Payment is not confirmed yet",
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("Error checking bank transaction: ", e);
            return new PaymentStatusResponse(
                    "ERROR",
                    "Unable to check payment status",
                    LocalDateTime.now()
            );
        }
    }
    private boolean isTransactionMatched(TransactionDetail transaction,
                                         QrTransactionEntity qrTransaction) {
        String expectedDescription = qrTransaction.getAddInfo(); // "Payment for: UUID"
        String actualDescription = transaction.getDescription();
        int paymentIndex = actualDescription.indexOf("Payment for ");

        String result = (paymentIndex != -1)
                ? actualDescription.substring(paymentIndex, Math.min(paymentIndex + 20, actualDescription.length()))
                : "";

        BigDecimal expectedAmount = qrTransaction.getPurchase().getPrice();
        BigDecimal actualAmount = transaction.getAmount();

        String expectedAccount = qrTransaction.getBankInfo().getAccountNumber();
        String actualAccount = transaction.getAccountNumber();

        boolean descriptionMatch = result != null &&
                result.trim().equals(expectedDescription.trim());
        boolean amountMatch = actualAmount.compareTo(expectedAmount) == 0;
        boolean accountMatch = actualAccount != null &&
                actualAccount.equals(expectedAccount);

        log.info("Transaction matching - Description: {}, Amount: {}, Account: {}",
                descriptionMatch, amountMatch, accountMatch);

        return descriptionMatch && amountMatch && accountMatch;
    }

    private LocalDateTime parseTransactionTime(String timeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception e) {
            log.warn("Unable to parse transaction time: {}", timeStr);
            return LocalDateTime.now();
        }
    }

    private void updatePurchaseStatus(PremiumPurchaseEntity purchase, String status) {
        purchase.setStatus(status);
        premiumPurchaseRepository.save(purchase);
        if("PAID".equals(status)) {
            updateUserPremiumStatus(purchase.getUserId(), 1);
        }
        log.info("Updated purchase {} status to {}", purchase.getId(), status);
    }

    private ResponseEntity<PaymentStatusResponse> monitorPaymentStatus(QrTransactionEntity qrTransaction) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = qrTransaction.getExpiresAt();

        while (LocalDateTime.now().isBefore(endTime)) {
            try {
                PaymentStatusResponse status = checkBankTransaction(qrTransaction);

                if ("PAID".equals(status.getStatus())) {
                    updatePurchaseStatus(qrTransaction.getPurchase(), "PAID");
                    return ResponseEntity.ok(status);
                }

                Thread.sleep(10000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Payment monitoring interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Error during payment monitoring", e);
            }
        }

        cancelExpiredTransaction(qrTransaction);
        return ResponseEntity.ok(new PaymentStatusResponse(
                "EXPIRED",
                "Payment timeout - QR code expired",
                LocalDateTime.now()
        ));
    }
    private void updateUserPremiumStatus(String userId, Integer premiumStatus) {
        String url = String.format(
                "https://user-service/api/users/update-premium-status/%s?premiumStatus=%d",
                userId, premiumStatus
        );
        restTemplate.postForEntity(url, null, Void.class);
    }
}
