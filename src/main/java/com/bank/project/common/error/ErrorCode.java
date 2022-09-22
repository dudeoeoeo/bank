package com.bank.project.common.error;

public enum ErrorCode {

    // ACCOUNT
    NOT_FOUND_ENTITY("A001", "Entity Not Found"),
    DUPLICATE_ENTITY("A002", "Entity Already Exists"),

    // BANK
    INVALID_ACCOUNT_NUMBER("B001", "Invalid Account Number"),
    INVALID_ACCOUNT_PASSWORD("B002", "Invalid Account Password"),
    BANK_SERVER_ERROR("B003", "Banking Not Response"),
    BALANCE_DEFICIENCY("B004", "Balance Is Not Enough"),
    INVALID_INQUIRY_KEY("B005", "The Remittance Key Is Not Invalid"),
    ;

    private final String code;
    private final String message;

    ErrorCode (String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
