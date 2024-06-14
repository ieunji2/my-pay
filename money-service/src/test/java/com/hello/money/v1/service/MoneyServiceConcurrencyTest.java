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
class MoneyServiceConcurrencyTest {

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
            arguments(
                    new ChargeMoneyServiceDto(1L, "이름", BigInteger.valueOf(2000), "적요"), 100, 200000));
  }

  private static Stream<Arguments> sendMoneyServiceDtoParam() {
    return Stream.of(
            arguments(
                    new SendMoneyServiceDto(1L, "이름", 2L, BigInteger.valueOf(100), "적요"), 100, 0, 10000));
  }

  @ParameterizedTest(name = "머니충전 {1}번 동시 요청 시 잔액이 {2}와 같다.")
  @MethodSource("chargeMoneyServiceDtoParam")
  public void 머니충전_동시_요청_후_잔액_확인(final ChargeMoneyServiceDto dto, int threadCount, int balance) throws InterruptedException {
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

  @ParameterizedTest(name = "머니송금 {1}번 동시 요청 시 잔액이 {2}와 같다.")
  @MethodSource("sendMoneyServiceDtoParam")
  public void 머니송금_동시_요청_후_잔액_확인(final SendMoneyServiceDto dto, int threadCount, int senderBalance, int receiverBalance) throws InterruptedException {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(dto.accountId()));
    senderWallet.addMoney(BigInteger.valueOf(10000));
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
    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());

    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(senderBalance));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(receiverBalance));
  }
}
