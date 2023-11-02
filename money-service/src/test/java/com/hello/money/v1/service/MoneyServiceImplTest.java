package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.repository.TransactionRepository;
import com.hello.money.v1.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
public class MoneyServiceImplTest {

  @Autowired
  private MoneyServiceImpl moneyService;

  @Autowired
  private WalletPort walletPort;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private MoneyExceptionTest exceptionTest;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  private static Stream<Arguments> accountParam() {
    return Stream.of(
            arguments(new Account(1L, "이름")));
  }

  private static Stream<Arguments> chargeMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요")));
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("지갑을 생성한다")
  void 지갑생성(final Account account) {
    //given, when
    final WalletResponse response = moneyService.createWallet(account);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("지갑 생성 후 계정 아이디로 지갑이 있는지 확인한다.")
  void 지갑_있는지_확인(final Account account) {
    //given
    walletPort.saveWallet(new Wallet(account.id()));

    //when
    final boolean exists = walletPort.existsWalletByAccountId(account.id());

    //then
    assertThat(exists).isTrue();
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("동일한 계정으로 지갑 중복 생성 시 예외가 발생한다.")
  void 지갑_중복_생성_예외_발생(final Account account) {
    //given
    moneyService.createWallet(account);

    //when, then
    assertThatThrownBy(() -> {
      moneyService.createWallet(account);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("지갑 생성 후 지갑을 조회한다.")
  void 지갑조회(final Account account) {
    //given
    moneyService.createWallet(account);

    //when
    final WalletResponse response = moneyService.getWallet(account);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("계정에 해당하는 지갑이 없으면 조회 시 예외가 발생한다.")
  void 지갑조회_예외_발생(final Account account) {
    //given, when, then
    assertThatThrownBy(() -> {
      moneyService.getWallet(account);
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("지갑 생성 후 금액을 충전한다.")
  void 머니충전(final Account account, final ChargeMoneyRequest request) {
    //given
    moneyService.createWallet(account);

    //when
    final WalletResponse response = moneyService.chargeMoney(account, request);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(request.amount());
  }

  @Test
  @DisplayName("충전 금액이 0보다 크지 않으면 ChargeMoneyRequest dto 생성 시 예외가 발생한다.")
  void ChargeMoneyRequest_생성_예외_발생() {
    //given
    final BigInteger amount = BigInteger.ZERO;

    //when, then
    assertThatThrownBy(() -> {
      new ChargeMoneyRequest(amount, "적요");
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("머니충전의 executeSave()에서 예외 발생 시 롤백된다.")
  void 머니충전_예외_발생_시_잔액_충전_안됨(final Account account, final ChargeMoneyRequest request) {
    //given
    moneyService.createWallet(account);

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    wallet.addMoney(request.amount());

    //when
    assertThatThrownBy(() -> {
      exceptionTest.executeCharge(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Rollback executeSave");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(BigInteger.ZERO);
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("동일한 Bean 안에서 상위 메서드에 Transactional 어노테이션이 없으면 하위에 선언되어 있어도 전이되지 않는다.")
  void 머니충전_예외_발생_시_잔액_충전_됨(final Account account, final ChargeMoneyRequest request) {
    //given
    moneyService.createWallet(account);

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    wallet.addMoney(request.amount());

    //when
    assertThatThrownBy(() -> {
      executeCharge(wallet);
    }).isInstanceOf(RuntimeException.class).hasMessage("Transactional not working");

    final Wallet savedWallet = walletPort.findWalletById(wallet.getId());

    //then
    assertThat(savedWallet.getBalance()).isEqualTo(request.amount());
  }

  @Transactional
  public void executeCharge(final Wallet wallet) {
    walletPort.saveWallet(wallet);
    throw new RuntimeException("Transactional not working");
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("지갑생성 및 머니충전 후 금액을 송금한다.")
  void 머니송금(final Account account, final SendMoneyRequest request) {
    //given
    moneyService.createWallet(account);
    moneyService.chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));

    final WalletResponse receiverWallet = moneyService.createWallet(new Account(2L, "이름2"));

    //when
    final WalletResponse senderWallet = moneyService.sendMoney(account, new SendMoneyRequest(
            receiverWallet.id(),
            request.amount(),
            request.summary()));

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.id());

    //then
    assertThat(senderWallet.balance()).isEqualTo(BigInteger.valueOf(1000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("머니송금의 executeSend()에서 예외 발생 시 롤백된다.")
  void 머니송금_예외_발생_시_잔액_변경_안됨(final Account account, final SendMoneyRequest request) {
    //given
    moneyService.createWallet(account);
    moneyService.chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));

    moneyService.createWallet(new Account(2L, "이름2"));

    final Wallet senderWallet = walletPort.findWalletByAccountId(account.id());
    final Wallet receiverWallet = walletPort.findWalletByAccountId(2L);

    senderWallet.subtractMoney(request.amount());
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

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("머니충전 동시 요청 시 race condition 발생으로 잔액이 기대와 다르다.")
  void 머니충전_동시에_1000번_요청_후_잔액_확인(final Account account, final ChargeMoneyRequest request) throws InterruptedException {
    //given
    moneyService.createWallet(account);

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          moneyService.chargeMoney(account, request);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    final WalletResponse wallet = moneyService.getWallet(account);
    System.out.println("wallet.balance() = " + wallet.balance());
    assertThat(wallet.balance()).isNotEqualTo(BigInteger.valueOf(3000000));
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("머니송금 동시 요청 시 race condition 발생으로 잔액이 기대와 다르다.")
  void 머니송금_동시에_1000번_요청_후_잔액_확인(final Account account, final SendMoneyRequest request) throws InterruptedException {
    //given
    moneyService.createWallet(account);
    moneyService.chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));

    final WalletResponse receiverWallet = moneyService.createWallet(new Account(2L, "이름2"));

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          moneyService.sendMoney(account, new SendMoneyRequest(
                  receiverWallet.id(),
                  BigInteger.ONE,
                  request.summary()));
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    final Wallet savedSenderWallet = walletPort.findWalletByAccountId(account.id());
    System.out.println("savedSenderWallet.getBalance() = " + savedSenderWallet.getBalance());

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.id());
    System.out.println("savedReceiverWallet.getBalance() = " + savedReceiverWallet.getBalance());

    assertThat(savedSenderWallet.getBalance()).isNotEqualTo(BigInteger.valueOf(2000));
    assertThat(savedReceiverWallet.getBalance()).isNotEqualTo(BigInteger.valueOf(1000));
  }
}
