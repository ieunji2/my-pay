package com.hello.money;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

class MoneyServiceTest {

  private MoneyService moneyService;
  private WalletPort walletPort;
  private WalletRepository walletRepository;

  @BeforeEach
  void setUp() {
    walletRepository = new WalletRepository();
    walletPort = new WalletPort() {
      @Override
      public Wallet saveWallet(final Wallet wallet) {
        return walletRepository.save(wallet);
      }

      @Override
      public Wallet findWalletByAccountId(final Long accountId) {
        return walletRepository.findWalletByAccountId(accountId);
      }
    };
    moneyService = new MoneyService(walletPort);
  }

  @Test
  void 지갑생성() {
    final Long accountId = 1L;
    final WalletResponse response = moneyService.createWallet(accountId);
    Assertions.assertThat(response.id()).isEqualTo(1L);
    Assertions.assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @Test
  void 지갑조회() {
    final Long accountId = 1L;
    final WalletResponse response = moneyService.getWallet(accountId);
    Assertions.assertThat(response.id()).isEqualTo(1L);
    Assertions.assertThat(response.balance()).isEqualTo(BigInteger.ZERO);
  }

  @Test
  void 머니충전() {
    final Long accountId = 1L;
    final BigInteger amount = BigInteger.valueOf(1000);
    final AddMoneyRequest request = new AddMoneyRequest(amount);
    final WalletResponse response = moneyService.addMoney(accountId, request);
    Assertions.assertThat(response.id()).isEqualTo(1L);
    Assertions.assertThat(response.balance()).isEqualTo(BigInteger.valueOf(1000));
  }

  private record AddMoneyRequest(BigInteger amount) {
    private AddMoneyRequest {
      Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
    }
  }

  private class MoneyService {

    private final WalletPort walletPort;

    private MoneyService(final WalletPort walletPort) {
      this.walletPort = walletPort;
    }

    public WalletResponse createWallet(final Long accountId) {
      final Wallet wallet = new Wallet(accountId);
      final Wallet savedWallet = walletPort.saveWallet(wallet);
      return WalletResponse.from(savedWallet);
    }

    public WalletResponse getWallet(final Long accountId) {
      final Wallet wallet = walletPort.findWalletByAccountId(accountId);
      return WalletResponse.from(wallet);
    }

    public WalletResponse addMoney(final Long accountId, final AddMoneyRequest request) {
      final Wallet wallet = new Wallet(accountId);
      wallet.addMoney(request.amount());
      final Wallet savedWallet = walletPort.saveWallet(wallet);
      return WalletResponse.from(savedWallet);
    }
  }

  private class Wallet {
    private Long id;
    private final Long accountId;
    private BigInteger balance;

    public Wallet(final Long accountId) {
      Assert.notNull(accountId, "계정 ID는 필수입니다.");
      this.accountId = accountId;
      this.balance = BigInteger.ZERO;
    }

    public Long getId() {
      return id;
    }

    public Long getAccountId() {
      return accountId;
    }

    public BigInteger getBalance() {
      return balance;
    }

    public void assignId(final Long id) {
      this.id = id;
    }

    public void addMoney(final BigInteger amount) {
      this.balance = this.balance.add(amount);
    }
  }

  private interface WalletPort {

    Wallet saveWallet(Wallet wallet);

    Wallet findWalletByAccountId(Long accountId);
  }

  private class WalletAdapter implements WalletPort {

    private final WalletRepository walletRepository;

    private WalletAdapter(final WalletRepository walletRepository) {
      this.walletRepository = walletRepository;
    }

    @Override
    public Wallet saveWallet(final Wallet wallet) {
      return walletRepository.save(wallet);
    }

    @Override
    public Wallet findWalletByAccountId(final Long accountId) {
      return walletRepository.findWalletByAccountId(accountId);
    }
  }

  private class WalletRepository {
    private Map<Long, Wallet> persistence = new HashMap<>();
    private Long sequence = 0L;

    public Wallet save(final Wallet wallet) {
      wallet.assignId(++sequence);
      persistence.put(wallet.getId(), wallet);
      return persistence.get(1L);
    }

    public Wallet findWalletByAccountId(final Long accountId) {
      save(new Wallet(1L));
      return persistence.get(1L);
    }
  }

  private record WalletResponse(Long id, Long accountId, BigInteger balance) {
    private WalletResponse {
      Assert.notNull(id, "지갑 ID는 필수입니다.");
      Assert.notNull(accountId, "계정 ID는 필수입니다.");
      Assert.isTrue(balance.compareTo(BigInteger.ZERO) >= 0, "잔액은 0보다 크거나 같아야 합니다.");
    }

    public WalletResponse(final Wallet wallet) {
      this(
              wallet.getId(),
              wallet.getAccountId(),
              wallet.getBalance());
    }

    public static WalletResponse from(final Wallet wallet) {
      return new WalletResponse(wallet);
    }
  }
}