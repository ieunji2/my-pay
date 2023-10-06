package com.hello.money.v1.repository;

import com.hello.money.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TransactionRepository {
  private Map<Long, Transaction> persistance = new HashMap<>();

  private Long sequence = 0L;

  public Transaction save(final Transaction transaction) {
    if (null == transaction.getId()) {
      transaction.assignId(++sequence);
    }
    persistance.put(transaction.getId(), transaction);
    return persistance.get(transaction.getId());
  }
}
