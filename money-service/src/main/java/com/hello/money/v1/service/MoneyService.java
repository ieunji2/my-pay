package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MoneyService {

  private final WalletPort walletPort;

  @Transactional
  public WalletResponse createWallet(final Long accountId) {
    checkWalletExists(accountId);
    final Wallet wallet = new Wallet(accountId);
    return WalletResponse.from(walletPort.saveWallet(wallet));
  }

  private void checkWalletExists(final Long accountId) {
    if (walletPort.existsWalletByAccountId(accountId)) {
      throw new IllegalArgumentException("해당 계정에 대한 지갑이 이미 존재합니다.");
    }
  }

  public WalletResponse getWallet(final Long accountId) {
    return WalletResponse.from(walletPort.findWalletByAccountId(accountId));
  }

  @Transactional
  public WalletResponse addMoney(final Long accountId, final AddMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    wallet.addMoney(request.amount());
    return WalletResponse.from(walletPort.saveWallet(wallet));
  }
}
