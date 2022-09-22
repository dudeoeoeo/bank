package com.bank.project.common.handler;

import com.bank.project.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {

    private ErrorCode errorCode;
    private String detailMessage;

    public AccountException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}
