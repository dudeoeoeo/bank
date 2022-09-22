package com.bank.project.business.dto.response;

import lombok.Getter;

@Getter
public class DepositRespDto {

    private String resultCode;
    private String userId;

    public DepositRespDto(String resultCode, String userId) {
        this.resultCode = resultCode;
        this.userId = userId;
    }
}
