package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;

public interface TransactionPort {
  Transaction saveTransaction(final Transaction transaction);
}
