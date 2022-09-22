package com.bank.project.business.entity;

import com.bank.project.business.constant.BankType;
import com.bank.project.common.constant.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class BankRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionKey;

    private String publicToken;

    private String privateToken;

    @Enumerated(EnumType.STRING)
    private BankType bankType;

    private String resultCode;

    private int balance;

    public static BankRecord createDepositBankRecord(String transactionKey, String publicToken,
                                                     String resultCode, int balance)
    {
        return BankRecord.builder()
                .transactionKey(transactionKey)
                .publicToken(publicToken)
                .bankType(BankType.DEPOSIT)
                .resultCode(resultCode)
                .balance(balance)
                .build();
    }

    public static BankRecord createWithdrawBankRecord(String transactionKey, String privateToken,
                                                      String resultCode, int balance)
    {
        return BankRecord.builder()
                .transactionKey(transactionKey)
                .privateToken(privateToken)
                .bankType(BankType.WITHDRAWAL)
                .resultCode(resultCode)
                .balance(balance)
                .build();
    }

    public static BankRecord createRemitBankRecord(String transactionKey, String publicToken,
                                                   String privateToken, String resultCode,
                                                   int balance)
    {
        return BankRecord.builder()
                .transactionKey(transactionKey)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .bankType(BankType.REMITTANCE)
                .resultCode(resultCode)
                .balance(balance)
                .build();
    }
}
