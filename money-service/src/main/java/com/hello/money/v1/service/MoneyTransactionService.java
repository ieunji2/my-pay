package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.TransactionStatus;
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
  public TransactionStatus executeCharge(final Wallet wallet, final Transaction transaction) {
    walletPort.saveWallet(wallet);
    transaction.success();
    return getTransactionStatus(transactionPort.saveTransaction(transaction));
  }

  @Transactional
  public TransactionStatus executeSend(
          final Wallet senderWallet,
          final Transaction senderTransaction,
          final Wallet receiverWallet,
          final Transaction receiverTransaction) {

    walletPort.saveWallet(senderWallet);
    senderTransaction.success();
    final TransactionStatus senderTransactionStatus = getTransactionStatus(transactionPort.saveTransaction(senderTransaction));

    walletPort.saveWallet(receiverWallet);
    receiverTransaction.success();

    final TransactionStatus receiverTransactionStatus = getTransactionStatus(transactionPort.saveTransaction(receiverTransaction));

    return isSuccessTransaction(senderTransactionStatus, receiverTransactionStatus)
            ? TransactionStatus.NORMAL
            : TransactionStatus.ERROR;
  }

  @Transactional
  public TransactionStatus saveFailedTransaction(final Transaction transaction) {
    transaction.fail();
    final Transaction savedTransaction = transactionPort.saveTransaction(transaction);
    return getTransactionStatus(savedTransaction);
  }

  private static TransactionStatus getTransactionStatus(final Transaction transaction) {
    return transaction.getTransactionStatus();
  }

  private static boolean isSuccessTransaction(final TransactionStatus senderTransactionStatus, final TransactionStatus receiverTransactionStatus) {
    return TransactionStatus.NORMAL.equals(senderTransactionStatus)
            && TransactionStatus.NORMAL.equals(receiverTransactionStatus);
  }
}
