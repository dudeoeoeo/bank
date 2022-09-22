package com.bank.project.business.dto.response;

import lombok.Getter;

@Getter
public class WithdrawalRespDto {

    private String resultCode;
    private String userId;
    private int balance;

    public WithdrawalRespDto(String resultCode, String userId, int balance) {
        this.resultCode = resultCode;
        this.userId = userId;
        this.balance = balance;
    }
}
