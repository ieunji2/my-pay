package com.hello.money.v1.repository;

import com.hello.money.domain.Wallet;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class WalletRepository {
  private Map<Long, Wallet> persistence = new HashMap<>();
  private Long sequence = 0L;

  public Wallet save(final Wallet wallet) {
    wallet.assignId(++sequence);
    persistence.put(wallet.getId(), wallet);
    return persistence.get(sequence);
  }

  public Wallet findWalletByAccountId(final Long accountId) {
    save(new Wallet(accountId));
    return persistence.get(sequence);
  }

  public void deleteAll() {
    persistence = new HashMap<>();
    sequence = 0L;
  }
}
