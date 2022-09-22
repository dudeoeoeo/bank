package com.bank.project.business.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DepositReqDto {

    @NotNull
    @NotEmpty
    private String publicToken;

    @Min(value = 100, message = "입금 최소 금액은 100원 이상입니다.")
    @NotNull
    private int depositBalance;
}
