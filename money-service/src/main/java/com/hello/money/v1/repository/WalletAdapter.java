package com.hello.money.v1.repository;

import com.hello.money.common.exception.WalletNotFoundException;
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
    return walletRepository.findByAccountId(accountId)
                           .orElseThrow(WalletNotFoundException::new);
  }

  @Override
  public boolean existsWalletByAccountId(final Long accountId) {
    return walletRepository.existsByAccountId(accountId);
  }

  @Override
  public Wallet findWalletById(final Long walletId) {
    return walletRepository.findById(walletId)
                           .orElseThrow(WalletNotFoundException::new);
  }
}
