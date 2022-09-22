package com.bank.project.service;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.constant.BankType;
import com.bank.project.business.dto.request.*;
import com.bank.project.business.dto.response.*;
import com.bank.project.business.entity.Account;
import com.bank.project.business.entity.AccountRepository;
import com.bank.project.business.entity.BankRecord;
import com.bank.project.business.entity.BankRecordRepository;
import com.bank.project.business.service.AccountServiceImpl;
import com.bank.project.business.service.BankRecordServiceImpl;
import com.bank.project.common.token.TokenEncoder;
import com.bank.project.common.token.TokenProvider;
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
public class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenEncoder tokenEncoder;

    @Mock
    private BankRecordServiceImpl bankRecordService;

    @Mock
    private BankRecordRepository bankRecordRepository;

    @Test
    public void 계좌등록_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        final Account account = Account.createAccount(userId, publicToken, privateToken);

        // stub
        AccountReqSaveDto reqDto = new AccountReqSaveDto();
        reqDto.setUserId(userId);
        when(accountRepository.save(any())).thenReturn(account);

        // when
        final AccountRespSaveDto dto = accountService.createAccount(reqDto);

        // then
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getPublicToken()).isEqualTo(publicToken);
        assertThat(dto.getPrivateToken()).isEqualTo(privateToken);
    }

    @Test
    public void 입금_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        final Account account = Account.createAccount(userId, publicToken, privateToken);

        int balance = 50000;
        String resultCode = String.valueOf(BankCode.BD00);

        // stub
        DepositReqDto reqDto = new DepositReqDto();
        reqDto.setPublicToken(publicToken);
        reqDto.setDepositBalance(balance);
        when(accountRepository.findByPublicToken(any())).thenReturn(Optional.of(account));

        // when
        final DepositRespDto respDto = accountService.depositBalance(reqDto);

        // then
        assertThat(respDto.getUserId()).isEqualTo(userId);
        assertThat(respDto.getResultCode()).isEqualTo(resultCode);
    }

    @Test
    public void 출금_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        int balance = 50000;
        final Account account = Account.builder()
                .userId(userId)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .balance(balance)
                .build();

        String resultCode = String.valueOf(BankCode.BW00);

        // stub
        WithdrawalReqDto reqDto = new WithdrawalReqDto();
        reqDto.setPrivateToken(privateToken);
        reqDto.setWithdrawalBalance(30000);
        when(accountRepository.findByPrivateToken(any())).thenReturn(Optional.ofNullable(account));

        // when
        final WithdrawalRespDto respDto = accountService.withdrawBalance(reqDto);

        // then
        assertThat(respDto.getResultCode()).isEqualTo(resultCode);
        assertThat(respDto.getUserId()).isEqualTo(userId);
        assertThat(respDto.getBalance()).isEqualTo(20000);
    }

    @Test
    public void 송금_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        int balance = 50000;

        String resultCode = String.valueOf(BankCode.BR00);
        String transactionKey = UUID.randomUUID().toString();

        // stub
        Account account = Account.builder()
                .userId(userId)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .balance(balance)
                .build();

        BankRecord bankRecord = BankRecord.builder()
                .transactionKey(transactionKey)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .bankType(BankType.REMITTANCE)
                .resultCode(resultCode)
                .balance(balance)
                .build();

        RemittanceReqDto reqDto = new RemittanceReqDto();
        reqDto.setPublicToken(publicToken);
        reqDto.setPrivateToken(privateToken);
        reqDto.setRemittanceBalance(balance);

        when(accountRepository.findByPublicToken(any())).thenReturn(Optional.ofNullable(account));
        when(accountRepository.findByPrivateToken(any())).thenReturn(Optional.ofNullable(account));
        when(bankRecordService.recordRemit(publicToken, privateToken, resultCode, balance)).thenReturn(transactionKey);

        // when
        final RemittanceRespDto respDto = accountService.remittanceBalance(reqDto);

        // then
        assertThat(respDto.getResultCode()).isEqualTo(bankRecord.getResultCode());
        assertThat(respDto.getTransactionKey()).isEqualTo(bankRecord.getTransactionKey());
    }

    @Test
    public void 송금결과조회_테스트() {
        // given
        String transactionKey = UUID.randomUUID().toString();
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        BankType bankType = BankType.WITHDRAWAL;
        String resultCode = String.valueOf(BankCode.BW00);
        int balance = 70000;

        final BankRecord bankRecord = BankRecord.builder()
                .transactionKey(transactionKey)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .bankType(bankType)
                .resultCode(resultCode)
                .balance(balance)
                .build();

        // stub
        when(bankRecordService.getRecord(any())).thenReturn(bankRecord.getResultCode());

        // when
        final InquiryRemittanceRespDto dto = accountService.inquiryRemittance(transactionKey);

        // then
        assertThat(dto.getResultCode()).isEqualTo(resultCode);
        assertThat(dto.getResultCode()).isEqualTo(bankRecord.getResultCode());
    }
}
