package com.example.paymentservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankTransactionResponse {
    private List<TransactionDetail> data;
    private boolean error;
}
