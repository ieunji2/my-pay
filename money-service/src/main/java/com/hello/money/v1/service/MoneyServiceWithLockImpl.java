package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Primary
public class MoneyServiceWithLockImpl implements MoneyService {

  private final MoneyDistributedLockService moneyDistributedLockService;
  private final WalletPort walletPort;

  @Override
  public WalletResponse createWallet(final Account account) {
    if (isExistsWallet(account.id())) {
      throw new IllegalArgumentException("해당 계정에 대한 지갑이 이미 존재합니다.");
    }
    final Wallet wallet = walletPort.saveWallet(new Wallet(account.id()));
    return WalletResponse.from(wallet);
  }

  @Override
  public WalletResponse getWallet(final Account account) {
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    return WalletResponse.from(wallet);
  }

  @Override
  public WalletResponse chargeMoney(final Account account, final ChargeMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    moneyDistributedLockService.chargeMoneyWithLock(account, request, wallet.getId());
    return getWallet(account);
  }

  @Override
  public WalletResponse sendMoney(final Account account, final SendMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    moneyDistributedLockService.sendMoneyWithLock(account, request, wallet.getId());
    return getWallet(account);
  }

  private boolean isExistsWallet(final Long accountId) {
    return walletPort.existsWalletByAccountId(accountId);
  }
}
