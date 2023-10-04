package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;

public interface WalletPort {

  Wallet saveWallet(final Wallet wallet);

  Wallet findWalletByAccountId(final Long accountId);
}
