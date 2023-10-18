package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.SaveMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.repository.TransactionRepository;
import com.hello.money.v1.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private MoneyTransactionServiceExceptionTest exceptionTest;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  private static Stream<Arguments> accountParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"))
    );
  }

  private static Stream<Arguments> saveMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SaveMoneyRequest(BigInteger.valueOf(3000), "적요"))
    );
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요"))
    );
  }

  void 지갑생성(final Account account) {
    //given, when
    final WalletResponse response = moneyService.createWallet(account);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  void 계정에_해당하는_지갑이_있는지_확인(final Account account) {
    //given
    지갑생성(account);

    //when
    final boolean existsed = walletPort.existsWalletByAccountId(account.id());

    //then
    assertThat(existsed).isTrue();
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  void 같은_계정으로_지갑_생성시_오류(final Account account) {
    //given
    지갑생성(account);

    //when, then
    assertThatThrownBy(() -> {
      지갑생성(account);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  void 지갑조회(final Account account) {
    //given
    지갑생성(account);

    //when
    final WalletResponse response = moneyService.getWallet(account);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  void 계정에_해당하는_지갑이_없으면_조회시_오류(final Account account) {
    //given, when, then
    assertThatThrownBy(() -> {
      moneyService.getWallet(account);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("saveMoneyRequestParam")
  void 머니충전(final Account account, final SaveMoneyRequest request) {
    //given
    지갑생성(account);

    //when
    final WalletResponse response = moneyService.saveMoney(account, request);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(request.amount());
  }

  @Test
  void 충전_금액이_0보다_크지_않으면_AddMoneyRequest_생성시_오류() {
    //given
    final BigInteger amount = BigInteger.ZERO;

    //when, then
    assertThatThrownBy(() -> {
      new SaveMoneyRequest(amount, "적요");
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("saveMoneyRequestParam")
  void 머니충전_오류_발생시_saveWallet_롤백(final Account account, final SaveMoneyRequest request) {
    //given
    지갑생성(account);

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    wallet.addMoney(request.amount());

    //when
    assertThatThrownBy(() -> {
      exceptionTest.executeSave(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Rollback executeSave");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("saveMoneyRequestParam")
  void 동일한_Bean_안에_상위_메서드가_Transactional_어노테이션이_없으면_하위에는_선언이_되어_있어도_전이되지_않는다(
          final Account account,
          final SaveMoneyRequest request) {
    //given
    지갑생성(account);

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    wallet.addMoney(request.amount());

    //when
    assertThatThrownBy(() -> {
      executeSave(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Transactional not working");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(request.amount());
  }

  @Transactional
  public void executeSave(final Wallet wallet) {
    walletPort.saveWallet(wallet);
    throw new RuntimeException("Transactional not working");
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  void 송금하기(final Account account, final SendMoneyRequest request) {
    //given
    머니충전(account, new SaveMoneyRequest(BigInteger.valueOf(3000), "적요"));
    지갑생성(new Account(2L, "이름2"));

    final Wallet receiverWallet = walletPort.findWalletByAccountId(2L);

    //when
    final WalletResponse senderWallet = moneyService.sendMoney(account, new SendMoneyRequest(
            receiverWallet.getId(),
            request.amount(),
            request.summary()));

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    //then
    assertThat(senderWallet.balance()).isEqualTo(BigInteger.valueOf(1000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  void 송금하기_오류_발생시_saveWallet_롤백(final Account account, final SendMoneyRequest request) {
    //given
    머니충전(account, new SaveMoneyRequest(BigInteger.valueOf(3000), "적요"));
    지갑생성(new Account(2L, "이름2"));

    final Wallet senderWallet = walletPort.findWalletByAccountId(account.id());
    final Wallet receiverWallet = walletPort.findWalletByAccountId(2L);

    senderWallet.addMoney(request.amount().negate());
    receiverWallet.addMoney(request.amount());

    //when
    assertThatThrownBy(() -> {
      exceptionTest.executeSend(senderWallet, receiverWallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Rollback executeSend");

    final Wallet savedSenderWallet = walletPort.findWalletById(senderWallet.getId());
    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    //then
    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(3000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }
}