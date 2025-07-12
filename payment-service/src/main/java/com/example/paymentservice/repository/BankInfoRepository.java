package com.example.paymentservice.repository;

import com.example.paymentservice.entity.BankInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankInfoRepository extends JpaRepository<BankInfoEntity,Long> {
}
