package com.hello.money.v1.dto;

import com.hello.money.domain.Wallet;
import org.springframework.util.Assert;

import java.math.BigInteger;

public record WalletResponse(Long id, Long accountId, BigInteger balance) {
  public WalletResponse {
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
