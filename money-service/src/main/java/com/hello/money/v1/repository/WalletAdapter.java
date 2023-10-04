package com.hello.money.v1.repository;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.service.WalletPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class WalletAdapter implements WalletPort {

  private final WalletRepository walletRepository;

  @Override
  public Wallet saveWallet(final Wallet wallet) {
    return walletRepository.save(wallet);
  }

  @Override
  public Wallet findWalletByAccountId(final Long accountId) {
    return walletRepository.findWalletByAccountId(accountId);
  }
}
