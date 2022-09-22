package com.bank.project.repository;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.entity.BankRecord;
import com.bank.project.business.entity.BankRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class BankRecordRepositoryTest {

    @Autowired
    private BankRecordRepository bankRecordRepository;

    @Test
    void 입금내역기록_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String resultCode = String.valueOf(BankCode.BD00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createDepositBankRecord(
                transactionKey, publicToken, resultCode, balance);

        // when
        BankRecord save = bankRecordRepository.save(bankRecord);

        // then
        assertThat(save.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(save.getPublicToken()).isEqualTo(publicToken);
        assertThat(save.getResultCode()).isEqualTo(resultCode);
        assertThat(save.getBalance()).isEqualTo(balance);
    }

    @Test
    void 출금내역기록_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BW00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createWithdrawBankRecord(
                transactionKey, privateToken, resultCode, balance);

        // when
        BankRecord save = bankRecordRepository.save(bankRecord);

        // then
        assertThat(save.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(save.getPrivateToken()).isEqualTo(privateToken);
        assertThat(save.getResultCode()).isEqualTo(resultCode);
        assertThat(save.getBalance()).isEqualTo(balance);
    }

    @Test
    void 송금내역기록_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BR00);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createRemitBankRecord(
                transactionKey, publicToken, privateToken, resultCode, balance);

        // when
        BankRecord save = bankRecordRepository.save(bankRecord);

        // then
        assertThat(save.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(save.getPublicToken()).isEqualTo(publicToken);
        assertThat(save.getPrivateToken()).isEqualTo(privateToken);
        assertThat(save.getResultCode()).isEqualTo(resultCode);
        assertThat(save.getBalance()).isEqualTo(balance);
    }

    @Test
    void 거래내역조회_테스트() {

        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "cXdra25kYXNsbmZwcWV3amdwaW9ua3BibXZucHdmbjtrYXNkO25mbmV3O29nbm9hZG5zYm9uZGZrdnNmZXc=";
        String privateToken = "cHJpdmF0ZS10b2tlbg==";
        String resultCode = String.valueOf(BankCode.BA02);
        int balance = 10000;

        BankRecord bankRecord = BankRecord.createRemitBankRecord(
                transactionKey, publicToken, privateToken, resultCode, balance);

        BankRecord save = bankRecordRepository.save(bankRecord);

        // when
        BankRecord findBankRecord = bankRecordRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new IllegalArgumentException("Entity Noy Found"));

        // then
        assertThat(findBankRecord.getTransactionKey()).isEqualTo(transactionKey);
        assertThat(findBankRecord.getPublicToken()).isEqualTo(publicToken);
        assertThat(findBankRecord.getPrivateToken()).isEqualTo(privateToken);
        assertThat(findBankRecord.getResultCode()).isEqualTo(resultCode);
        assertThat(findBankRecord.getBalance()).isEqualTo(balance);
    }
}
