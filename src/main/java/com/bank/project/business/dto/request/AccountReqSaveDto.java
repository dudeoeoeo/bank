package com.bank.project.business.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
public class AccountReqSaveDto {

    @Size(min = 4, max = 50, message = "id 는 최소 4자 최대 50자 입니다.")
    @NotBlank
    private String userId;
}
