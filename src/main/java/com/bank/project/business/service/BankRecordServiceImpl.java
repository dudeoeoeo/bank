package com.bank.project.business.service;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.entity.BankRecord;
import com.bank.project.business.entity.BankRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@Service
public class BankRecordServiceImpl implements BankRecordService {

    BankRecordRepository bankRecordRepository;

    @Autowired
    public BankRecordServiceImpl(BankRecordRepository bankRecordRepository) {
        this.bankRecordRepository = bankRecordRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDeposit(String publicToken, String resultCode, int balance) {
        String transactionKey = UUID.randomUUID().toString();

        final BankRecord bankRecord = BankRecord.createDepositBankRecord(transactionKey, publicToken,
                                                                        resultCode, balance);
        log.info("[recordDeposit] record: {}", bankRecord);

        bankRecordRepository.save(bankRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordWithdraw(String privateToken, String resultCode, int balance) {
        String transactionKey = UUID.randomUUID().toString();

        final BankRecord bankRecord = BankRecord.createWithdrawBankRecord(transactionKey, privateToken,
                                                                        resultCode, balance);
        log.info("[recordWithdraw] record: {}", bankRecord);

        bankRecordRepository.save(bankRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String recordRemit(String publicToken, String privateToken, String resultCode, int balance) {
        String transactionKey = UUID.randomUUID().toString();

        final BankRecord bankRecord =
                BankRecord.createRemitBankRecord(transactionKey, publicToken, privateToken, resultCode, balance);

        final BankRecord saved = bankRecordRepository.save(bankRecord);

        log.info("[recordRemit] record: {}", saved);

        return saved.getTransactionKey();
    }

    @Override
    public String getRecord(String transactionKey) {
        log.info("[getRecord] transactionKey: {}", transactionKey);

        Optional<BankRecord> bankRecord = bankRecordRepository.findByTransactionKey(transactionKey);

        if (bankRecord.isEmpty()) {
            return String.valueOf(BankCode.BR90);
        }

        return bankRecord.get().getResultCode();
    }
}
