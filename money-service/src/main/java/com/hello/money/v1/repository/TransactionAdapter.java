package com.hello.money.v1.repository;

import com.hello.money.domain.Transaction;
import com.hello.money.v1.service.TransactionPort;
import org.springframework.stereotype.Component;

@Component
class TransactionAdapter implements TransactionPort {

  private final TransactionRepository transactionRepository;

  TransactionAdapter(final TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @Override
  public Transaction saveTransaction(final Transaction transaction) {
    return transactionRepository.save(transaction);
  }
}
