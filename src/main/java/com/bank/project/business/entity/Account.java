package com.bank.project.business.entity;

import com.bank.project.common.constant.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String userId;

    @Column(unique = true)
    private String publicToken;

    @Column(unique = true)
    private String privateToken;

    private int balance;

    public static Account createAccount(String userId, String publicToken, String privateToken) {
        return Account.builder()
                .userId(userId)
                .publicToken(publicToken)
                .privateToken(privateToken)
                .balance(0)
                .build();
    }

    public void deposit(int balance) {
        this.balance += balance;
    }

    public void withdraw(int balance) {
        this.balance -= balance;
    }
}
