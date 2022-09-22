package com.bank.project.business.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRecordRepository extends JpaRepository<BankRecord, Long> {

    Optional<BankRecord> findByTransactionKey(String transactionKey);
}
