package com.bank.project.repository;

import com.bank.project.business.entity.Account;
import com.bank.project.business.entity.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void 계좌생성_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        final Account account = Account.createAccount(userId, publicToken, privateToken);

        // when
        final Account save = accountRepository.save(account);

        // then
        assertThat(save.getUserId()).isEqualTo("tera_kay");
        assertThat(save.getPublicToken()).isEqualTo("123-1231435-2563");
        assertThat(save.getPrivateToken()).isEqualTo("11234");
    }

    @Test
    void 계좌조회_테스트() {
        // given
        String userId = "tera_kay";
        String publicToken = "123-1231435-2563";
        String privateToken = "11234";
        final Account account = Account.createAccount(userId, publicToken, privateToken);

        accountRepository.save(account);

        // when
        final Optional<Account> saved = accountRepository.findByUserId(userId);

        // then
        assertTrue(saved.isPresent());
        assertThat(userId).isEqualTo(saved.get().getUserId());
        assertThat(publicToken).isEqualTo(saved.get().getPublicToken());
        assertThat(privateToken).isEqualTo(saved.get().getPrivateToken());
    }
}
