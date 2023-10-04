package com.hello.money;

import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.repository.WalletRepository;
import com.hello.money.v1.service.MoneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MoneyServiceTest {

  @Autowired
  private MoneyService moneyService;

  @Autowired
  private WalletRepository walletRepository;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
  }

  @Test
  void 지갑생성() {
    final Long accountId = 1L;

    final WalletResponse response = moneyService.createWallet(accountId);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @Test
  void 지갑조회() {
    final Long accountId = 1L;

    final WalletResponse response = moneyService.getWallet(accountId);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @Test
  void 머니충전() {
    final Long accountId = 1L;
    final BigInteger amount = BigInteger.valueOf(1000);
    final AddMoneyRequest request = new AddMoneyRequest(amount);

    final WalletResponse response = moneyService.addMoney(accountId, request);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(amount);
  }
}