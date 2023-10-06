package com.hello.money;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.AddMoneyRequest;
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
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
class MoneyServiceTest {

  @Autowired
  private MoneyService moneyService;

  @Autowired
  private WalletPort walletPort;

  @Autowired
  private WalletRepository walletRepository;

  private TransactionPort transactionPort;
  private Wallet receiverWallet;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();

    TransactionRepository transactionRepository = new TransactionRepository();
    transactionPort = new TransactionAdapter(transactionRepository);
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

  @ParameterizedTest
  @MethodSource("addMoneyRequestParam")
  void 충전_거래생성(final Long accountId, final AddMoneyRequest request) {
    //given
    지갑생성(accountId);

    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    wallet.addMoney(request.amount());

    final Transaction transaction = new Transaction(wallet, request.amount(), request.summary());
    transactionPort.saveTransaction(transaction);

    try {
      walletPort.saveWallet(wallet);
      transaction.success();
    } catch (RuntimeException e) {
      transaction.fail();
    } finally {
      transactionPort.saveTransaction(transaction);
    }
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  void 송금_거래생성(final Long senderAccountId, final SendMoneyRequest request) {
    //given
    //지갑생성(senderAccountId);
    머니충전(senderAccountId, new AddMoneyRequest(BigInteger.valueOf(3000), "적요"));
    지갑생성(2L);

    checkBalance(senderAccountId, request);

    checkReceiver(request);

    final Wallet senderWallet = walletPort.findWalletByAccountId(senderAccountId);
    senderWallet.addMoney(request.amount().negate());

    final Transaction senderTransaction = transactionPort.saveTransaction(
            new Transaction(
                    senderWallet,
                    request.receiverWalletId(),
                    request.amount(),
                    request.summary(),
                    "출금"));

    final Wallet receiverWallet = walletPort.findWalletById(request.receiverWalletId());
    receiverWallet.addMoney(request.amount());

    final Transaction receiverTransaction = transactionPort.saveTransaction(
            new Transaction(
                    receiverWallet,
                    request.receiverWalletId(),
                    request.amount(),
                    request.summary(),
                    "입금"));

    try {
      walletPort.saveWallet(senderWallet);
      walletPort.saveWallet(receiverWallet);
      senderTransaction.success();
      receiverTransaction.success();
    } catch (RuntimeException e) {
      senderTransaction.fail();
      receiverTransaction.fail();
    } finally {
      transactionPort.saveTransaction(senderTransaction);
      transactionPort.saveTransaction(receiverTransaction);
    }
  }

  private void checkBalance(final Long senderAccountId, final SendMoneyRequest request) {
    final Wallet senderWallet = walletPort.findWalletByAccountId(senderAccountId);

    if (request.amount().compareTo(senderWallet.getBalance()) > 0) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }
  }

  private void checkReceiver(final SendMoneyRequest request) {
    if (!walletPort.existsWalletById(request.receiverWalletId())) {
      new IllegalArgumentException("수취인의 지갑 ID가 존재하지 않습니다.");
    }

    final AccountResponse receiver = getReceiverAccount(request.receiverWalletId());
    if (receiver == null || !receiver.isValid()) {
      new IllegalArgumentException("수취인의 계정으로 송금할 수 없습니다.");
    }
  }

  private AccountResponse getReceiverAccount(final Long receiverWalletId) {
    return new AccountResponse(2L, "receiver", "receiver@mymoney.com", true);
  }

  private class Transaction {
    private Long id;
    private Wallet wallet;
    private Long receiverWalletId;
    private BigInteger amount;
    private String summary;
    private String transactionType;

    private String transactionStatus;

    public Transaction(
            final Wallet wallet,
            final BigInteger amount,
            final String summary) {
      Assert.notNull(wallet, "지갑은 필수입니다.");
      this.wallet = wallet;
      this.receiverWalletId = wallet.getId();
      this.amount = amount;
      this.summary = summary;
      this.transactionType = "입금";
      this.transactionStatus = "요청";
    }

    public Transaction(
            final Wallet wallet,
            final Long receiverWalletId,
            final BigInteger amount,
            final String summary,
            final String transactionType) {
      Assert.notNull(wallet, "지갑은 필수입니다.");
      Assert.notNull(receiverWalletId, "수취인 지갑 ID는 필수입니다.");
      Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
      Assert.hasText(transactionType, "거래 유형은 필수입니다.");
      this.wallet = wallet;
      this.receiverWalletId = receiverWalletId;
      this.amount = amount;
      this.summary = summary;
      this.transactionType = transactionType;
      this.transactionStatus = "요청";
    }

    public void success() {
      this.transactionStatus = "정상";
    }

    public void fail() {
      this.transactionStatus = "오류";
    }

    public void assignId(final Long id) {
      this.id = id;
    }

    public Long getId() {
      return this.id;
    }

  }

  private interface TransactionPort {
    Transaction saveTransaction(final Transaction transaction);
  }

  private class TransactionAdapter implements TransactionPort {

    private final TransactionRepository transactionRepository;

    private TransactionAdapter(final TransactionRepository transactionRepository) {
      this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction saveTransaction(final Transaction transaction) {
      return transactionRepository.save(transaction);
    }

  }

  private class TransactionRepository {
    private Map<Long, Transaction> persistance = new HashMap<>();

    private Long sequence = 0L;

    public Transaction save(final Transaction transaction) {
      if (null == transaction.getId()) {
        transaction.assignId(++sequence);
      }
      persistance.put(transaction.getId(), transaction);
      return persistance.get(transaction.getId());
    }

  }

  private record SendMoneyRequest(Long receiverWalletId, BigInteger amount, String summary) {
    private SendMoneyRequest {
      Assert.notNull(receiverWalletId, "수취인 지갑 ID는 필수입니다.");
      Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
    }
  }
}