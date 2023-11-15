package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
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
class MoneyServiceWithLockTest {

  @Autowired
  private MoneyService moneyService;

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

  private static Stream<Arguments> chargeMoneyServiceDtoParam() {
    return Stream.of(
            arguments(new ChargeMoneyServiceDto(1L, "이름", BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyServiceDtoParam() {
    return Stream.of(
            arguments(new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요")));
  }

  private static Stream<Arguments> chargeMoneyAndSendMoneyServiceDtoParam() {
    return Stream.of(
            arguments(
                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요"),
                    new ChargeMoneyServiceDto(2L, "이름2", BigInteger.valueOf(3000), "적요"),
                    new SendMoneyServiceDto(3L, "이름", 1L, BigInteger.ONE, "적요")));
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyServiceDtoParam")
  @DisplayName("@DistributedLock 적용한 머니충전 동시 요청 시 잔액이 기대와 같다.")
  public void 분산_락_머니충전_동시에_1000번_요청_후_잔액_확인(final ChargeMoneyServiceDto dto) throws InterruptedException {
    //given
    walletPort.saveWallet(new Wallet(dto.accountId()));

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          moneyService.chargeMoney(dto);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    assertThat(wallet.getBalance()).isEqualTo(3000000);
  }

  @ParameterizedTest
  @MethodSource("sendMoneyServiceDtoParam")
  @DisplayName("@DistributedMultiLock 적용한 머니송금 동시 요청 시 잔액이 기대와 같다.")
  public void 분산_락_머니송금_동시에_1000번_요청_후_잔액_확인(final SendMoneyServiceDto dto) throws InterruptedException {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(dto.accountId()));
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
          moneyService.sendMoney(
                  new SendMoneyServiceDto(
                          dto.accountId(),
                          dto.accountName(),
                          receiverWallet.getId(),
                          dto.amount(),
                          dto.summary()));
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    final Wallet savedSenderWallet = walletPort.findWalletByAccountId(dto.accountId());
    System.out.println("savedSenderWallet.getBalance() = " + savedSenderWallet.getBalance());

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());
    System.out.println("savedReceiverWallet.getBalance() = " + savedReceiverWallet.getBalance());

    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(1000));
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyAndSendMoneyServiceDtoParam")
  @DisplayName("@DistributedMultiLock 적용한 머니충전 및 머니송금 동시 요청 시 잔액이 기대와 같다.")
  void 분산_락_머니충전_머니송금_동시에_300번_요청_후_잔액_확인(
          final SendMoneyServiceDto dto1,
          final ChargeMoneyServiceDto dto2,
          final SendMoneyServiceDto dto3) throws InterruptedException {

    //given: 1(3000), 2(3000), 3(3000)
    final Wallet wallet1 = walletPort.saveWallet(new Wallet(dto1.accountId()));
    wallet1.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet1);

    final Wallet wallet2 = walletPort.saveWallet(new Wallet(dto2.accountId()));
    wallet2.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet2);

    final Wallet wallet3 = walletPort.saveWallet(new Wallet(dto3.accountId()));
    wallet3.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet3);

    //when: 1->100->2, 2->300000->2, 3->100->1
    final int threadCount = 100;
    final ExecutorService executorService1 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService2 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService3 = Executors.newFixedThreadPool(16);
    final CountDownLatch latch = new CountDownLatch(300);

    for (int i = 0; i < threadCount; i++) {
      executorService1.submit(() -> {
        try {
          moneyService.sendMoney(
                  new SendMoneyServiceDto(
                          dto1.accountId(),
                          dto1.accountName(),
                          wallet2.getId(),
                          dto1.amount(),
                          dto1.summary()));
        } finally {
          latch.countDown();
        }
      });
      executorService2.submit(() -> {
        try {
          moneyService.chargeMoney(dto2);
        } finally {
          latch.countDown();
        }
      });
      executorService3.submit(() -> {
        try {
          moneyService.sendMoney(
                  new SendMoneyServiceDto(
                          dto3.accountId(),
                          dto3.accountName(),
                          wallet1.getId(),
                          dto3.amount(),
                          dto3.summary()));
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then: 1(3000), 2(303100), 3(2900)
    final Wallet savedWallet1 = walletPort.findWalletByAccountId(dto1.accountId());
    System.out.println("savedWallet1.getBalance() = " + savedWallet1.getBalance());

    final Wallet savedWallet2 = walletPort.findWalletByAccountId(dto2.accountId());
    System.out.println("savedWallet2.getBalance() = " + savedWallet2.getBalance());

    final Wallet savedWallet3 = walletPort.findWalletByAccountId(dto3.accountId());
    System.out.println("savedWallet3.getBalance() = " + savedWallet3.getBalance());

    assertThat(savedWallet1.getBalance()).isEqualTo(BigInteger.valueOf(3000));
    assertThat(savedWallet2.getBalance()).isEqualTo(BigInteger.valueOf(303100));
    assertThat(savedWallet3.getBalance()).isEqualTo(BigInteger.valueOf(2900));
  }
}
