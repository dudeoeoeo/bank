package com.bank.project.business.service;

import com.bank.project.business.constant.BankCode;
import com.bank.project.business.dto.request.*;
import com.bank.project.business.dto.response.*;
import com.bank.project.business.entity.Account;
import com.bank.project.business.entity.AccountRepository;
import com.bank.project.common.error.ErrorCode;
import com.bank.project.common.handler.AccountException;
import com.bank.project.common.token.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    AccountRepository accountRepository;
    TokenProvider tokenProvider;
    BankRecordService bankRecordService;
    ExecutorService executor = Executors.newFixedThreadPool(4);

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              TokenProvider tokenProvider,
                              BankRecordService bankRecordService)
    {
        this.accountRepository = accountRepository;
        this.tokenProvider = tokenProvider;
        this.bankRecordService = bankRecordService;
    }

    @Override
    @Transactional
    public AccountRespSaveDto createAccount(AccountReqSaveDto dto) {
        Optional<Account> findAccount = accountRepository.findByUserId(dto.getUserId());

        if (findAccount.isPresent()) {
            throw new AccountException(ErrorCode.DUPLICATE_ENTITY, "해당 계좌가 이미 존재합니다.");
        }

        final String publicToken = tokenProvider.createPublicToken(dto.getUserId());
        final String privateToken = tokenProvider.createPrivateToken(dto.getUserId());

        final Account account = Account.createAccount(dto.getUserId(), publicToken, privateToken);

        final Account savedAccount = accountRepository.save(account);

        return new AccountRespSaveDto(savedAccount);
    }

    @Override
    @Transactional
    public DepositRespDto depositBalance(DepositReqDto dto) {
        Optional<Account> findAccount = accountRepository.findByPublicToken(dto.getPublicToken());

        String resultCode = "";

        try {
            resultCode = deposit(findAccount, dto.getDepositBalance());
        } catch (Exception e) {
            log.warn("예금 실패 errorCode: {}, request: {}", e.getMessage(), dto);
            resultCode = e.getMessage();
        }

        bankRecordService.recordDeposit(dto.getPublicToken(), resultCode, dto.getDepositBalance());

        // 계좌 조회 실패
        if (resultCode.equals(String.valueOf(BankCode.BA00))) {
            return new DepositRespDto(resultCode, dto.getPublicToken());
        }

        return new DepositRespDto(resultCode, findAccount.get().getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WithdrawalRespDto withdrawBalance(WithdrawalReqDto dto) {
        Optional<Account> findAccount = accountRepository.findByPrivateToken(dto.getPrivateToken());

        String resultCode = "";

        try {
            resultCode = withdraw(findAccount, dto.getWithdrawalBalance());
        } catch (Exception e) {
            log.warn("출금 실패 errorCode: {}, request: {}", e.getMessage(), dto);
            resultCode = e.getMessage();
        }

        bankRecordService.recordWithdraw(dto.getPrivateToken(), resultCode, dto.getWithdrawalBalance());

        // 계좌 비밀번호 틀림
        if (resultCode.equals(String.valueOf(BankCode.BA01))) {
            return new WithdrawalRespDto(resultCode, dto.getPrivateToken(), dto.getWithdrawalBalance());
        }

        return new WithdrawalRespDto(resultCode, findAccount.get().getUserId(), findAccount.get().getBalance());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RemittanceRespDto remittanceBalance(RemittanceReqDto dto) {

        String resultCode = "";

        try {
            resultCode = remittance(dto);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.warn("송금 실패 errorCode: {}, request: ", e.getMessage(), dto);
            resultCode = e.getMessage();
        }

        final String transactionKey = bankRecordService.recordRemit(
                dto.getPublicToken(), dto.getPrivateToken(), resultCode, dto.getRemittanceBalance());

        return new RemittanceRespDto(resultCode, transactionKey);
    }

    @Override
    public InquiryRemittanceRespDto inquiryRemittance(String transactionKey) {

        if (transactionKey == null || transactionKey.equals("")) {
            throw new AccountException(ErrorCode.INVALID_INQUIRY_KEY, "거래 식별 키 값이 없습니다.");
        }

        final String resultCode = bankRecordService.getRecord(transactionKey);

        return new InquiryRemittanceRespDto(resultCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String remittance(RemittanceReqDto dto) {

        final Optional<Account> depositWithdraw = accountRepository.findByPublicToken(dto.getPublicToken());
        final Optional<Account> withdrawAccount = accountRepository.findByPrivateToken(dto.getPrivateToken());

        CompletableFuture<String> depositResult = depositAsync(depositWithdraw, dto);
        CompletableFuture<String> withdrawalResult = withdrawalAsync(withdrawAccount, dto);

        final String resultCode = depositResult.thenCombine(withdrawalResult, (d, w) -> getResultCode(d, w)).join();

        log.info("remittance resultCode: {}", resultCode);

        if (resultCode.equals(String.valueOf(BankCode.BR00)) == false) {
            log.warn("resultCode not BR00 => {}", resultCode);
            throw new RuntimeException(resultCode);
        }

        return resultCode;
    }

    public String deposit(Optional<Account> account, int balance) {
        if (account.isEmpty()) {
            throw new RuntimeException(String.valueOf(BankCode.BA00));
        }

        try {
            account.get().deposit(balance);
        } catch (Exception e) {
            log.warn("deposit exception: {}", e.getMessage());
            e.printStackTrace();
            return String.valueOf(BankCode.BA02);
        }

        return String.valueOf(BankCode.BD00);
    }

    public String withdraw(Optional<Account> account, int balance) {
        if (account.isEmpty()) {
            throw new RuntimeException(String.valueOf(BankCode.BA01));
        }

        if (account.get().getBalance() < balance) {
            throw new RuntimeException(String.valueOf(BankCode.BW90));
        }

        try {
            account.get().withdraw(balance);
        } catch (Exception e) {
            log.warn("withdraw exception: {}", e.getMessage());
            e.printStackTrace();
            return String.valueOf(BankCode.BA02);
        }

        return String.valueOf(BankCode.BW00);
    }

    public CompletableFuture<String> depositAsync(Optional<Account> depositAccount, RemittanceReqDto dto) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return deposit(depositAccount, dto.getRemittanceBalance());
            } catch (Exception e) {
                return e.getMessage();
            }
        }, executor);
    }

    public CompletableFuture<String> withdrawalAsync(Optional<Account> withdrawalAccount, RemittanceReqDto dto) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return withdraw(withdrawalAccount, dto.getRemittanceBalance());
            } catch (Exception e) {
                return e.getMessage();
            }
        }, executor);
    }

    public String getResultCode(String depositResult, String withdrawalResult) {
        if (depositResult.equals(String.valueOf(BankCode.BD00)) && withdrawalResult.equals(String.valueOf(BankCode.BW00)))
            return String.valueOf(BankCode.BR00);
        else if (depositResult.equals(String.valueOf(BankCode.BD00)) == false)
            return depositResult;
        else
            return withdrawalResult;
    }
}
