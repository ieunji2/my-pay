package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MoneyService {

  private final WalletPort walletPort;

  public WalletResponse createWallet(final Long accountId) {
    final Wallet wallet = new Wallet(accountId);
    return WalletResponse.from(walletPort.saveWallet(wallet));
  }

  public WalletResponse getWallet(final Long accountId) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    return WalletResponse.from(wallet);
  }

  public WalletResponse addMoney(final Long accountId, final AddMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    wallet.addMoney(request.amount());
    return WalletResponse.from(walletPort.saveWallet(wallet));
  }
}
