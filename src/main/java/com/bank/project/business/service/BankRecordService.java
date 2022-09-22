package com.bank.project.business.service;

public interface BankRecordService {

    void recordDeposit(String publicToken, String resultCode, int balance);
    void recordWithdraw(String privateToken, String resultCode, int balance);
    String recordRemit(String publicToken, String privateToken, String resultCode, int balance);
    String getRecord(String transactionKey);
}
