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
    return walletRepository.findByAccountId(accountId)
                           .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
  }

  @Override
  public boolean existsWalletByAccountId(final Long accountId) {
    return walletRepository.existsByAccountId(accountId);
  }

  @Override
  public boolean existsWalletById(final Long walletId) {
    return walletRepository.existsById(walletId);
  }

  @Override
  public Wallet findWalletById(final Long walletId) {
    return walletRepository.findById(walletId)
                           .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));
  }
}
