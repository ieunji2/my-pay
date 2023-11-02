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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
class MoneyServiceWithLockImplTest {

  @Autowired
  private MoneyServiceWithLockImpl moneyServiceWithLock;

  @Autowired
  private WalletPort walletPort;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  private static Stream<Arguments> chargeMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(2L, BigInteger.ONE, "적요")));
  }

  private static Stream<Arguments> chargeMoneyAndSendMoneyRequestParam() {
    return Stream.of(
            arguments(
                    new Account(1L, "이름"),
                    new Account(2L, "이름2"),
                    new Account(3L, "이름3"),
                    new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"),
                    new SendMoneyRequest(2L, BigInteger.ONE, "적요")));
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("@DistributedLock 적용한 머니충전 서비스를 호출한다.")
  void 분산_락_머니충전(final Account account, final ChargeMoneyRequest request) {
    //given
    walletPort.saveWallet(new Wallet(account.id()));

    //when
    final WalletResponse response = moneyServiceWithLock.chargeMoney(account, request);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(request.amount());
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("@DistributedLock 적용한 머니충전 동시 요청 시 잔액이 기대와 같다.")
  public void 분산_락_머니충전_동시에_1000번_요청_후_잔액_확인(final Account account, final ChargeMoneyRequest request) throws InterruptedException {
    //given
    walletPort.saveWallet(new Wallet(account.id()));

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          moneyServiceWithLock.chargeMoney(account, request);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    assertThat(wallet.getBalance()).isEqualTo(3000000);
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("@DistributedMultiLock 적용한 머니송금 서비스를 호출한다.")
  void 분산_락_머니송금(final Account account, final SendMoneyRequest request) {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(account.id()));
    senderWallet.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(senderWallet);

    final Wallet receiverWallet = walletPort.saveWallet(new Wallet(2L));

    //when
    final WalletResponse savedSenderWallet = moneyServiceWithLock.sendMoney(account, new SendMoneyRequest(
            receiverWallet.getId(),
            request.amount(),
            request.summary()));

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    //then
    assertThat(savedSenderWallet.balance()).isEqualTo(2999);
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(1);
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("@DistributedMultiLock 적용한 머니송금 동시 요청 시 잔액이 기대와 같다.")
  public void 분산_락_머니송금_동시에_1000번_요청_후_잔액_확인(final Account account, final SendMoneyRequest request) throws InterruptedException {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(account.id()));
    senderWallet.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(senderWallet);

    final Wallet receiverWallet = walletPort.saveWallet(new Wallet(2L));

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          moneyServiceWithLock.sendMoney(account, new SendMoneyRequest(
                  receiverWallet.getId(),
                  request.amount(),
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

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());
    System.out.println("savedReceiverWallet.getBalance() = " + savedReceiverWallet.getBalance());

    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(1000));
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyAndSendMoneyRequestParam")
  @DisplayName("@DistributedMultiLock 적용한 머니충전 및 머니송금 동시 요청 시 잔액이 기대와 같다.")
  void 분산_락_머니충전_머니송금_동시에_300번_요청_후_잔액_확인(
          final Account account1,
          final Account account2,
          final Account account3,
          final ChargeMoneyRequest chargeMoneyRequest,
          final SendMoneyRequest sendMoneyRequest) throws InterruptedException {

    //given: 1(3000), 2(3000), 3(3000)
    final Wallet wallet1 = walletPort.saveWallet(new Wallet(account1.id()));
    wallet1.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet1);

    final Wallet wallet2 = walletPort.saveWallet(new Wallet(account2.id()));
    wallet2.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet2);

    final Wallet wallet3 = walletPort.saveWallet(new Wallet(account3.id()));
    wallet3.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet3);

    //when: 1->100->2, 2->300000>2, 3->100->1
    final int threadCount = 100;
    final ExecutorService executorService1 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService2 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService3 = Executors.newFixedThreadPool(16);
    final CountDownLatch latch = new CountDownLatch(300);

    for (int i = 0; i < threadCount; i++) {
      executorService1.submit(() -> {
        try {
          moneyServiceWithLock.sendMoney(account1, new SendMoneyRequest(
                  wallet2.getId(),
                  sendMoneyRequest.amount(),
                  sendMoneyRequest.summary()));
        } finally {
          latch.countDown();
        }
      });
      executorService2.submit(() -> {
        try {
          moneyServiceWithLock.chargeMoney(account2, chargeMoneyRequest);
        } finally {
          latch.countDown();
        }
      });
      executorService3.submit(() -> {
        try {
          moneyServiceWithLock.sendMoney(account3, new SendMoneyRequest(
                  wallet1.getId(),
                  sendMoneyRequest.amount(),
                  sendMoneyRequest.summary()));
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then: 1(3000), 2(303100), 3(2900)
    final Wallet savedWallet1 = walletPort.findWalletByAccountId(account1.id());
    System.out.println("savedWallet1.getBalance() = " + savedWallet1.getBalance());

    final Wallet savedWallet2 = walletPort.findWalletByAccountId(account2.id());
    System.out.println("savedWallet2.getBalance() = " + savedWallet2.getBalance());

    final Wallet savedWallet3 = walletPort.findWalletByAccountId(account3.id());
    System.out.println("savedWallet3.getBalance() = " + savedWallet3.getBalance());

    assertThat(savedWallet1.getBalance()).isEqualTo(BigInteger.valueOf(3000));
    assertThat(savedWallet2.getBalance()).isEqualTo(BigInteger.valueOf(303100));
    assertThat(savedWallet3.getBalance()).isEqualTo(BigInteger.valueOf(2900));
  }
}