package com.example.paymentservice.service;

import com.example.paymentservice.model.request.BankInfoRequest;
import com.example.paymentservice.model.response.BankInfoResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BankInfoService {
    ResponseEntity<String> addBankInfo(BankInfoRequest bankInfoRequest);

    ResponseEntity<BankInfoResponse> getBankInfo(Long id);

    ResponseEntity<List<BankInfoResponse>> getAllBankInfo();

    ResponseEntity<String> updateBankInfo(Long id, BankInfoRequest bankInfoRequest);

    ResponseEntity<String> deleteBankInfo(Long id);
}
