package com.bank.project.business.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class RemittanceReqDto {

    @NotNull
    @NotEmpty
    private String publicToken;

    @NotNull
    @NotEmpty
    private String privateToken;

    @Min(value = 100, message = "송금 최소 금액은 100원 이상입니다.")
    private int remittanceBalance;
}
