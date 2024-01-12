package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;

public interface WalletPort {

  Wallet saveWallet(final Wallet wallet);

  Wallet findWalletByAccountId(final Long accountId);

  boolean existsWalletByAccountId(final Long accountId);

  Wallet findWalletById(final Long walletId);
}
