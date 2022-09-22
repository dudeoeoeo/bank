package com.bank.project.business.dto.response;

import com.bank.project.business.entity.Account;
import lombok.Getter;

@Getter
public class AccountRespSaveDto {

    private String userId;
    private String publicToken;
    private String privateToken;

    public AccountRespSaveDto(Account account) {
        this.userId = account.getUserId();
        this.publicToken = account.getPublicToken();
        this.privateToken = account.getPrivateToken();
    }
}
