package com.bank.project.business.dto.response;

import lombok.Getter;

@Getter
public class InquiryRemittanceRespDto {

    private String resultCode;

    public InquiryRemittanceRespDto(String resultCode) {
        this.resultCode = resultCode;
    }
}
