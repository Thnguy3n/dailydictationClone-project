package com.example.paymentservice.controller;

import com.example.paymentservice.model.request.BankInfoRequest;
import com.example.paymentservice.model.response.BankInfoResponse;
import com.example.paymentservice.service.BankInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment/bank-info")
@RequiredArgsConstructor
public class BankInfoController {
    private final BankInfoService bankInfoService;
    @PostMapping()
    public ResponseEntity<String> addBankInfo(@RequestBody BankInfoRequest bankInfoRequest) {
        return bankInfoService.addBankInfo(bankInfoRequest);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BankInfoResponse> getBankInfo(@PathVariable Long id) {
        return bankInfoService.getBankInfo(id);
    }
    @GetMapping
    public ResponseEntity<List<BankInfoResponse>> getAllBankInfo() {
        return bankInfoService.getAllBankInfo();
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBankInfo(@PathVariable Long id,
                                                 @Valid @RequestBody BankInfoRequest bankInfoRequest) {
        return bankInfoService.updateBankInfo(id, bankInfoRequest);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBankInfo(@PathVariable Long id) {
        return bankInfoService.deleteBankInfo(id);
    }
}
