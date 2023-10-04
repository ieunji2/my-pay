package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;

public interface WalletPort {

  Wallet saveWallet(Wallet wallet);

  Wallet findWalletByAccountId(Long accountId);
}
