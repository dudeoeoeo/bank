package com.bank.project.business.service;

import com.bank.project.business.dto.request.*;
import com.bank.project.business.dto.response.*;

public interface AccountService {

    AccountRespSaveDto createAccount(AccountReqSaveDto dto);
    DepositRespDto depositBalance(DepositReqDto dto);
    WithdrawalRespDto withdrawBalance(WithdrawalReqDto dto);
    RemittanceRespDto remittanceBalance(RemittanceReqDto dto);
    InquiryRemittanceRespDto inquiryRemittance(String transactionKey);
}
