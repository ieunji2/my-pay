package com.hello.money.v1.repository;

import com.hello.money.domain.Transaction;
import com.hello.money.v1.service.TransactionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class TransactionAdapter implements TransactionPort {

  private final TransactionRepository transactionRepository;

  @Override
  public Transaction saveTransaction(final Transaction transaction) {
    return transactionRepository.save(transaction);
  }
}
