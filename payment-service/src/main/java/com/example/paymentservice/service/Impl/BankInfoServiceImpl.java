package com.example.paymentservice.service.Impl;

import com.example.paymentservice.entity.BankInfoEntity;
import com.example.paymentservice.model.request.BankInfoRequest;
import com.example.paymentservice.model.response.BankInfoResponse;
import com.example.paymentservice.repository.BankInfoRepository;
import com.example.paymentservice.service.BankInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankInfoServiceImpl implements BankInfoService {
    private final BankInfoRepository bankInfoRepository;
    @Override
    public ResponseEntity<String> addBankInfo(BankInfoRequest bankInfoRequest) {
        try {
            BankInfoEntity bankInfoEntity = BankInfoEntity.builder()
                    .accountName(bankInfoRequest.getAccountName())
                    .acqId(bankInfoRequest.getAcqId())
                    .accountNumber(bankInfoRequest.getAccountNumber())
                    .format("text")
                    .template(bankInfoRequest.getTemplate())
                    .build();
            bankInfoRepository.save(bankInfoEntity);
            return ResponseEntity.ok("Bank information added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding bank information: " + e.getMessage());
        }
    }
    @Override
    public ResponseEntity<BankInfoResponse> getBankInfo(Long id) {
        try {
            BankInfoEntity entity = bankInfoRepository.findById(id)
                    .orElse(null);

            if (entity == null) {
                return ResponseEntity.notFound().build();
            }

            BankInfoResponse response = BankInfoResponse.builder()
                    .accountName(entity.getAccountName())
                    .accountNumber(entity.getAccountNumber())
                    .acqId(entity.getAcqId())
                    .template(entity.getTemplate())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    @Override
    public ResponseEntity<List<BankInfoResponse>> getAllBankInfo() {
        try {
            List<BankInfoEntity> entities = bankInfoRepository.findAll();

            List<BankInfoResponse> responses = entities.stream()
                    .map(entity -> BankInfoResponse.builder()
                            .accountName(entity.getAccountName())
                            .accountNumber(entity.getAccountNumber())
                            .acqId(entity.getAcqId())
                            .template(entity.getTemplate())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    @Override
    public ResponseEntity<String> updateBankInfo(Long id, BankInfoRequest bankInfoRequest) {
        try {
            BankInfoEntity entity = bankInfoRepository.findById(id)
                    .orElse(null);

            if (entity == null) {
                return ResponseEntity.notFound().build();
            }

            entity.setAccountName(bankInfoRequest.getAccountName());
            entity.setAcqId(bankInfoRequest.getAcqId());
            entity.setAccountNumber(bankInfoRequest.getAccountNumber());
            entity.setTemplate(bankInfoRequest.getTemplate());

            bankInfoRepository.save(entity);
            return ResponseEntity.ok("Bank information updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating bank information: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> deleteBankInfo(Long id) {
        try {
            if (!bankInfoRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            bankInfoRepository.deleteById(id);
            return ResponseEntity.ok("Bank information deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting bank information: " + e.getMessage());
        }
    }

}
