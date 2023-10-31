package com.hello.money.v1.facade;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.SaveMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.repository.TransactionRepository;
import com.hello.money.v1.repository.WalletRepository;
import com.hello.money.v1.service.WalletPort;
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
class RedissonLockMoneyFacadeTest {

  @Autowired
  private RedissonLockMoneyFacade redissonLockMoneyFacade;

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

  private static Stream<Arguments> saveMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SaveMoneyRequest(BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(2L, BigInteger.ONE, "적요")));
  }

  @ParameterizedTest
  @MethodSource("saveMoneyRequestParam")
  @DisplayName("분산 락을 구현한 머니충전 서비스를 호출한다.")
  void 분산_락_머니충전(final Account account, final SaveMoneyRequest request) {
    //given
    walletPort.saveWallet(new Wallet(account.id()));

    //when
    final WalletResponse response = redissonLockMoneyFacade.saveMoney(account, request);

    //then
    assertThat(response.accountId()).isEqualTo(account.id());
    assertThat(response.balance()).isEqualTo(request.amount());
  }

  @ParameterizedTest
  @MethodSource("saveMoneyRequestParam")
  @DisplayName("분산 락을 구현한 머니충전 동시 요청 시 잔액이 기대와 같다.")
  public void 분산_락_머니충전_동시에_1000번_요청_후_잔액_확인(final Account account, final SaveMoneyRequest request) throws InterruptedException {
    //given
    walletPort.saveWallet(new Wallet(account.id()));

    //when
    final int threadCount = 1000;
    final ExecutorService executorService = Executors.newFixedThreadPool(32);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          redissonLockMoneyFacade.saveMoney(account, request);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    //then
    System.out.println("unavailableCount = " + redissonLockMoneyFacade.getUnavailableSaveMoneyCount());

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    assertThat(wallet.getBalance()).isEqualTo(3000000);
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("분산 락을 구현한 머니송금 서비스를 호출한다.")
  void 분산_락_머니송금(final Account account, final SendMoneyRequest request) {
    //given
    final Wallet senderWallet = walletPort.saveWallet(new Wallet(account.id()));
    senderWallet.addMoney(BigInteger.valueOf(3000));
    walletPort.saveWallet(senderWallet);

    final Wallet receiverWallet = walletPort.saveWallet(new Wallet(2L));

    //when
    final WalletResponse savedSenderWallet = redissonLockMoneyFacade.sendMoney(account, new SendMoneyRequest(
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
  @DisplayName("분산 락을 구현한 머니송금 동시 요청 시 잔액이 기대와 같다.")
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
          redissonLockMoneyFacade.sendMoney(account, new SendMoneyRequest(
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
    System.out.println("unavailableCount = " + redissonLockMoneyFacade.getUnavailableSendMoneyCount());

    final Wallet savedSenderWallet = walletPort.findWalletByAccountId(account.id());
    System.out.println("savedSenderWallet.getBalance() = " + savedSenderWallet.getBalance());

    final Wallet savedReceiverWallet = walletPort.findWalletById(receiverWallet.getId());
    System.out.println("savedReceiverWallet.getBalance() = " + savedReceiverWallet.getBalance());

    assertThat(savedSenderWallet.getBalance()).isEqualTo(BigInteger.valueOf(2000));
    assertThat(savedReceiverWallet.getBalance()).isEqualTo(BigInteger.valueOf(1000));
  }
}
