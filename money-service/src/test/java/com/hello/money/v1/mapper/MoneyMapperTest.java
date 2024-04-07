package com.hello.money.v1.mapper;

import com.hello.money.config.EmbeddedRedisConfig;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.controller.mapper.MoneyMapper;
import com.hello.money.v1.dto.*;
import com.hello.money.v1.service.WalletPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EmbeddedRedisConfig.class)
class MoneyMapperTest {

  @Autowired
  private MoneyMapper mapper;

  @Autowired
  private WalletPort walletPort;

  @Test
  @DisplayName("Account 객체를 CreateWalletServiceDto 객체에 매핑한다.")
  void toCreateWalletServiceDto() {
    //given
    final Account account = new Account(1L, "이름");

    //when
    final CreateWalletServiceDto dto = mapper.toCreateWalletServiceDto(account);

    //then
    assertThat(dto.accountId()).isEqualTo(account.id());
    assertThat(dto.accountName()).isEqualTo(account.name());
  }

  @Test
  @DisplayName("Account 객체를 GetWalletServiceDto 객체에 매핑한다.")
  void toGetWalletServiceDto() {
    //given
    final Account account = new Account(1L, "이름");

    //when
    final GetWalletServiceDto dto = mapper.toGetWalletServiceDto(account);

    //then
    assertThat(dto.accountId()).isEqualTo(account.id());
    assertThat(dto.accountName()).isEqualTo(account.name());
  }

  @Test
  @DisplayName("Account 객체와 ChargeMoneyRequest 객체를 ChargeMoneyServiceDto 객체에 매핑한다.")
  void toChargeMoneyServiceDto() {
    //given
    final Account account = new Account(1L, "이름");
    final ChargeMoneyRequest request = new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요");

    //when
    final ChargeMoneyServiceDto dto = mapper.toChargeMoneyServiceDto(account, request);

    //then
    assertThat(dto.accountId()).isEqualTo(account.id());
    assertThat(dto.accountName()).isEqualTo(account.name());
    assertThat(dto.amount()).isEqualTo(request.amount());
    assertThat(dto.summary()).isEqualTo(request.summary());
  }

  @Test
  @DisplayName("Account 객체와 SendMoneyRequest 객체를 SendMoneyServiceDto 객체에 매핑한다.")
  void toSendMoneyServiceDto() {
    //given
    final Account account = new Account(1L, "이름");
    final SendMoneyRequest request = new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요");

    //when
    final SendMoneyServiceDto dto = mapper.toSendMoneyServiceDto(account, request);

    //then
    assertThat(dto.accountId()).isEqualTo(account.id());
    assertThat(dto.accountName()).isEqualTo(account.name());
    assertThat(dto.receiverWalletId()).isEqualTo(request.receiverWalletId());
    assertThat(dto.amount()).isEqualTo(request.amount());
    assertThat(dto.summary()).isEqualTo(request.summary());
  }

  @Test
  @DisplayName("Wallet 객체를 WalletResponse 객체에 매핑한다.")
  void toWalletResponse() {
    //given
    final Wallet wallet = new Wallet(1L);
    final Wallet savedWallet = walletPort.saveWallet(wallet);

    //when
    final WalletResponse response = mapper.toWalletResponse(savedWallet);

    //then
    assertThat(response.id()).isEqualTo(wallet.getId());
    assertThat(response.accountId()).isEqualTo(wallet.getAccountId());
    assertThat(response.balance()).isEqualTo(wallet.getBalance());
  }
}