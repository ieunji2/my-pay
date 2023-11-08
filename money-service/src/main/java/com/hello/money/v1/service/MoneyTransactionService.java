package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MoneyTransactionService {

  private final WalletPort walletPort;
  private final TransactionPort transactionPort;

  @Transactional
  public void executeCharge(final Wallet wallet, final Transaction transaction) {
    walletPort.saveWallet(wallet);
    transaction.success();
    transactionPort.saveTransaction(transaction);
  }

  @Transactional
  public void executeSend(
          final Wallet senderWallet,
          final Transaction senderTransaction,
          final Wallet receiverWallet,
          final Transaction receiverTransaction) {

    walletPort.saveWallet(senderWallet);
    senderTransaction.success();
    transactionPort.saveTransaction(senderTransaction);

    walletPort.saveWallet(receiverWallet);
    receiverTransaction.success();
    transactionPort.saveTransaction(receiverTransaction);
  }
}
