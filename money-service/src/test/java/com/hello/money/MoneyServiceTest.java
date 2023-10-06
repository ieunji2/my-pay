package com.hello.money;

import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.repository.WalletRepository;
import com.hello.money.v1.service.MoneyService;
import com.hello.money.v1.service.WalletPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
public class MoneyServiceTest {

  @Autowired
  private MoneyService moneyService;

  @Autowired
  private WalletPort walletPort;

  @Autowired
  private WalletRepository walletRepository;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
  }

  private static Stream<Arguments> accountIdParam() {
    return Stream.of(
            arguments(1L)
    );
  }

  private static Stream<Arguments> addMoneyRequestParam() {
    return Stream.of(
            arguments(1L, new AddMoneyRequest(BigInteger.valueOf(3000), "적요"))
    );
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(1L, new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요"))
    );
  }

  void 지갑생성(final Long accountId) {
    //given, when
    final WalletResponse response = moneyService.createWallet(accountId);

    //then
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  void 계정에_해당하는_지갑이_있는지_확인(final Long accountId) {
    //given
    지갑생성(accountId);

    //when
    final boolean existsed = walletPort.existsWalletByAccountId(accountId);

    //then
    assertThat(existsed).isTrue();
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  void 같은_계정으로_지갑_생성시_오류(final Long accountId) {
    //given
    지갑생성(accountId);

    //when, then
    assertThatThrownBy(() -> {
      지갑생성(accountId);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  void 지갑조회(final Long accountId) {
    //given
    지갑생성(accountId);

    //when
    final WalletResponse response = moneyService.getWallet(accountId);

    //then
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  void 계정에_해당하는_지갑이_없으면_조회시_오류(final Long accountId) {
    //given, when, then
    assertThatThrownBy(() -> {
      moneyService.getWallet(accountId);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("addMoneyRequestParam")
  void 머니충전(final Long accountId, final AddMoneyRequest request) {
    //given
    지갑생성(accountId);

    //when
    final WalletResponse response = moneyService.addMoney(accountId, request);

    //then
    assertThat(response.accountId()).isEqualTo(accountId);
    assertThat(response.balance()).isEqualTo(request.amount());
  }

  @Test
  void 충전_금액이_0보다_크지_않으면_AddMoneyRequest_생성시_오류() {
    //given
    final BigInteger amount = BigInteger.ZERO;

    //when, then
    assertThatThrownBy(() -> {
      new AddMoneyRequest(amount, "적요");
    }).isInstanceOf(IllegalArgumentException.class);
  }
}