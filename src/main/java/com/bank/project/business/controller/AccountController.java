package com.bank.project.business.controller;

import com.bank.project.business.dto.request.AccountReqSaveDto;
import com.bank.project.business.dto.request.DepositReqDto;
import com.bank.project.business.dto.request.RemittanceReqDto;
import com.bank.project.business.dto.request.WithdrawalReqDto;
import com.bank.project.business.dto.response.*;
import com.bank.project.business.service.AccountService;
import com.bank.project.common.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/v1")
@RestController
public class AccountController {

    AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // 계좌 생성
    @PostMapping("/account")
    public ResponseEntity createAccount(@RequestBody @Valid AccountReqSaveDto dto,
                                        BindingResult bindingResult)
    {
        final AccountRespSaveDto respSaveDto = accountService.createAccount(dto);
        final CommonResponse body = CommonResponse.success(respSaveDto);
        return new ResponseEntity(body, HttpStatus.CREATED);
    }

    // 입금
    @PostMapping("/account/deposit")
    public ResponseEntity deposit(@RequestBody @Valid DepositReqDto dto,
                                  BindingResult bindingResult)
    {
        final DepositRespDto respDto = accountService.depositBalance(dto);
        final CommonResponse body = CommonResponse.success(respDto);
        return new ResponseEntity(body, HttpStatus.OK);
    }

    // 출금
    @PostMapping("/account/withdraw")
    public ResponseEntity withdraw(@RequestBody @Valid WithdrawalReqDto dto,
                                   BindingResult bindingResult)
    {
        final WithdrawalRespDto respDto = accountService.withdrawBalance(dto);
        final CommonResponse body = CommonResponse.success(respDto);
        return new ResponseEntity(body, HttpStatus.OK);
    }

    // 송금
    @PostMapping("/account/remit")
    public ResponseEntity remit(@RequestBody @Valid RemittanceReqDto dto,
                                BindingResult bindingResult)
    {
        final RemittanceRespDto respDto = accountService.remittanceBalance(dto);
        final CommonResponse body = CommonResponse.success(respDto);
        return new ResponseEntity(body, HttpStatus.OK);
    }

    // 송금 조회
    @GetMapping("/account/{transactionKey}")
    public ResponseEntity inquiryRemittance(@PathVariable("transactionKey") String transactionKey)
    {
        final InquiryRemittanceRespDto respDto = accountService.inquiryRemittance(transactionKey);
        final CommonResponse body = CommonResponse.success(respDto);
        return new ResponseEntity(body, HttpStatus.OK);
    }
}
