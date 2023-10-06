package com.hello.money.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

  @Test
  @DisplayName("잔액을 더한다.")
  void addMoney() {
    //given
    final Wallet wallet = new Wallet(1L);
    assertThat(wallet.getBalance()).isEqualTo(BigInteger.ZERO);

    //when
    wallet.addMoney(BigInteger.valueOf(3000));

    //then
    assertThat(wallet.getBalance()).isEqualTo(BigInteger.valueOf(3000));
  }

  @Test
  void negatedMoney() {
    //given
    final BigInteger money = BigInteger.valueOf(1000);

    //when
    final BigInteger negatedMoney = money.negate();

    //then
    assertThat(negatedMoney).isEqualTo(BigInteger.valueOf(-1000));
  }
}