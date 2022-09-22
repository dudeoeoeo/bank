package com.bank.project.service;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.entity.BankRecord;
import com.bank.project.business.entity.BankRecordRepository;
import com.bank.project.business.service.BankRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankRecordServiceTest {

    @InjectMocks
    private BankRecordServiceImpl bankRecordService;

    @Mock
    private BankRecordRepository bankRecordRepository;

    @Test
    void 입금내역저장_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String resultCode = String.valueOf(BankCode.BD00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createDepositBankRecord(
                transactionKey, publicToken, resultCode, balance);

        // stub
        when(bankRecordRepository.findByTransactionKey(any())).thenReturn(Optional.ofNullable(bankRecord));

        bankRecordService.recordDeposit(publicToken, resultCode, balance);

        // when
        BankRecord findBankRecord = bankRecordRepository.findByTransactionKey(transactionKey)
                        .orElseThrow(() -> new IllegalArgumentException("Entity Noy Found"));

        // then
        assertThat(findBankRecord.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(findBankRecord.getPublicToken()).isEqualTo(publicToken);
        assertThat(findBankRecord.getResultCode()).isEqualTo(resultCode);
        assertThat(findBankRecord.getBalance()).isEqualTo(balance);
    }

    @Test
    void 출금내역저장_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BW00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createWithdrawBankRecord(
                transactionKey, privateToken, resultCode, balance);

        // stub
        when(bankRecordRepository.findByTransactionKey(any())).thenReturn(Optional.ofNullable(bankRecord));

        bankRecordService.recordWithdraw(privateToken, resultCode, balance);

        // when
        BankRecord findBankRecord = bankRecordRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new IllegalArgumentException("Entity Noy Found"));

        // then
        assertThat(findBankRecord.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(findBankRecord.getPrivateToken()).isEqualTo(privateToken);
        assertThat(findBankRecord.getResultCode()).isEqualTo(resultCode);
        assertThat(findBankRecord.getBalance()).isEqualTo(balance);
    }

    @Test
    void 송금내역저장_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BR00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createRemitBankRecord(
                transactionKey, publicToken, privateToken, resultCode, balance);

        // stub
        when(bankRecordRepository.save(any())).thenReturn(bankRecord);

        // when
        String responseTransactionKey =
                bankRecordService.recordRemit(publicToken, privateToken, resultCode, balance);

        // then
        assertThat(responseTransactionKey).isEqualTo(transactionKey);
    }

    @Test
    void 거래내역조회_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BR00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createRemitBankRecord(
                transactionKey, publicToken, privateToken, resultCode, balance);

        // stub
        when(bankRecordRepository.findByTransactionKey(any())).thenReturn(Optional.ofNullable(bankRecord));

        // when
        String responseResultCode = bankRecordService.getRecord(transactionKey);

        // then
        assertThat(responseResultCode).isEqualTo(resultCode);
    }
}
