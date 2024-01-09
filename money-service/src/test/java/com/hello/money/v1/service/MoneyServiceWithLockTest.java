package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import com.hello.money.v1.repository.TransactionRepository;
import com.hello.money.v1.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@SpringBootTest
class MoneyServiceWithLockTest {

  @MockBean
  private ExchangeApi exchangeApi;

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
//            arguments(
//                    new ChargeMoneyServiceDto(1L, "이름", BigInteger.valueOf(3000), "적요"), 1000, 3000000),
            arguments(
                    new ChargeMoneyServiceDto(1L, "이름", BigInteger.valueOf(3000), "적요"), 100, 300000));
  }

  private static Stream<Arguments> sendMoneyServiceDtoParam() {
    return Stream.of(
//            arguments(
//                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요"), 1000, 0, 1000),
            arguments(
                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요"), 100, 900, 100));
  }

  private static Stream<Arguments> chargeMoneyAndSendMoneyServiceDtoParam() {
    return Stream.of(
//            arguments(
//                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요"),
//                    new ChargeMoneyServiceDto(2L, "이름2", BigInteger.valueOf(3000), "적요"),
//                    new SendMoneyServiceDto(3L, "이름", 1L, BigInteger.ONE, "적요"),
//                    300,
//                    3000,
//                    303100,
//                    2900),
            arguments(
                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.ONE, "적요"),
                    new ChargeMoneyServiceDto(2L, "이름2", BigInteger.valueOf(3000), "적요"),
                    new SendMoneyServiceDto(3L, "이름", 1L, BigInteger.ONE, "적요"),
                    30,
                    3000,
                    33010,
                    2990));
  }

  @ParameterizedTest(name = "@DistributedLock 적용한 머니충전 {1}번 동시 요청 시 잔액이 {2}와 같다.")
  @MethodSource("chargeMoneyServiceDtoParam")
  public void 분산_락_머니충전_동시_요청_후_잔액_확인(final ChargeMoneyServiceDto dto, int threadCount, int balance) throws InterruptedException {
    //given
    walletPort.saveWallet(new Wallet(dto.accountId()));

    //when
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
    assertThat(wallet.getBalance()).isEqualTo(balance);
  }

  @ParameterizedTest(name = "@DistributedMultiLock 적용한 머니송금 {1}번 동시 요청 시 송금인 잔액이 {2}와 같다.")
  @MethodSource("sendMoneyServiceDtoParam")
  public void 분산_락_머니송금_동시_요청_후_잔액_확인(final SendMoneyServiceDto dto, int threadCount, int senderBalance, int receiverBalance) throws InterruptedException {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(dto.accountId()));
    senderWallet.addMoney(BigInteger.valueOf(1000));
    walletPort.saveWallet(senderWallet);

    final Wallet receiverWallet = walletPort.saveWallet(new Wallet(2L));

    when(exchangeApi.getAccount(2L))
            .thenReturn(new AccountResponse(2L, "이름2", "mypay@test.com", true));

    //when
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

    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(senderBalance));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(receiverBalance));
  }

  @ParameterizedTest(name = "@DistributedMultiLock 적용한 머니충전 및 머니송금 {3}번 동시 요청 시 잔액이 {4}, {5}, {6}와 같다.")
  @MethodSource("chargeMoneyAndSendMoneyServiceDtoParam")
  void 분산_락_머니충전_머니송금_동시_요청_후_잔액_확인(
          final SendMoneyServiceDto dto1,
          final ChargeMoneyServiceDto dto2,
          final SendMoneyServiceDto dto3,
          int threadCount,
          int balance1,
          int balance2,
          int balance3) throws InterruptedException {

    //given
    final Wallet wallet1 = walletPort.saveWallet(new Wallet(dto1.accountId()));
    wallet1.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet1);

    final Wallet wallet2 = walletPort.saveWallet(new Wallet(dto2.accountId()));
    wallet2.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet2);

    final Wallet wallet3 = walletPort.saveWallet(new Wallet(dto3.accountId()));
    wallet3.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(wallet3);

    when(exchangeApi.getAccount(1L))
            .thenReturn(new AccountResponse(1L, "이름1", "mypay@test.com", true));

    when(exchangeApi.getAccount(2L))
            .thenReturn(new AccountResponse(2L, "이름2", "mypay@test.com", true));

    //when
    final ExecutorService executorService1 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService2 = Executors.newFixedThreadPool(16);
    final ExecutorService executorService3 = Executors.newFixedThreadPool(16);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount / 3; i++) {
      executorService1.submit(() -> {
        try {
          //1->2
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
          //2->2
          moneyService.chargeMoney(dto2);
        } finally {
          latch.countDown();
        }
      });
      executorService3.submit(() -> {
        try {
          //3->1
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

    //then
    final Wallet savedWallet1 = walletPort.findWalletByAccountId(dto1.accountId());
    System.out.println("savedWallet1.getBalance() = " + savedWallet1.getBalance());

    final Wallet savedWallet2 = walletPort.findWalletByAccountId(dto2.accountId());
    System.out.println("savedWallet2.getBalance() = " + savedWallet2.getBalance());

    final Wallet savedWallet3 = walletPort.findWalletByAccountId(dto3.accountId());
    System.out.println("savedWallet3.getBalance() = " + savedWallet3.getBalance());

    assertThat(savedWallet1.getBalance()).isEqualTo(BigInteger.valueOf(balance1));
    assertThat(savedWallet2.getBalance()).isEqualTo(BigInteger.valueOf(balance2));
    assertThat(savedWallet3.getBalance()).isEqualTo(BigInteger.valueOf(balance3));
  }
}
