package com.hello.money.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

  @Test
  @DisplayName("금액을 더한다.")
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
  @DisplayName("금액을 뺀다.")
  void subtractMoney() {
    //given
    final Wallet wallet = new Wallet(1L);
    wallet.addMoney(BigInteger.valueOf(3000));

    //when
    wallet.subtractMoney(BigInteger.valueOf(2000));

    //then
    assertThat(wallet.getBalance()).isEqualTo(BigInteger.valueOf(1000));
  }

  @Test
  @DisplayName("잔액보다 큰 금액을 빼는 경우 예외가 발생한다.")
  void subtractMoneyFail() {
    //given
    final Wallet wallet = new Wallet(1L);

    //when, then
    assertThatThrownBy(() -> {
      wallet.subtractMoney(BigInteger.valueOf(2000));
    }).isInstanceOf(IllegalArgumentException.class).hasMessage("잔액이 부족합니다.");
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