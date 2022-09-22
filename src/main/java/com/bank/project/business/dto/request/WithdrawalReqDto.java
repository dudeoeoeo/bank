package com.bank.project.business.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class WithdrawalReqDto {

    @NotEmpty
    @NotNull
    private String privateToken;

    @Min(value = 100, message = "최소 출금 금액은 100원 이상입니다.")
    private int withdrawalBalance;
}
