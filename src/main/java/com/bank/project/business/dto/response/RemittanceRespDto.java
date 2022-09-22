package com.bank.project.business.dto.response;

import lombok.Getter;

@Getter
public class RemittanceRespDto {

    private String resultCode;
    private String transactionKey;

    public RemittanceRespDto(String resultCode, String transactionKey) {
        this.resultCode = resultCode;
        this.transactionKey = transactionKey;
    }
}
