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
    walletPort.saveWallet(receiverWallet);
    receiverTransaction.success();

    return isSuccessTransaction(
            getTransactionStatus(transactionPort.saveTransaction(senderTransaction)),
            getTransactionStatus(transactionPort.saveTransaction(receiverTransaction)))
            ? TransactionStatus.NORMAL
            : TransactionStatus.ERROR;
  }

  public TransactionStatus saveFailedTransaction(final Transaction transaction) {
    transaction.fail();
    return getTransactionStatus(transactionPort.saveTransaction(transaction));
  }

  @Transactional
  public TransactionStatus saveFailedTransaction(final Transaction senderTransaction, final Transaction receiverTransaction) {
    senderTransaction.fail();
    receiverTransaction.fail();
    getTransactionStatus(transactionPort.saveTransaction(senderTransaction));
    getTransactionStatus(transactionPort.saveTransaction(receiverTransaction));
    return TransactionStatus.ERROR;
  }

  private static TransactionStatus getTransactionStatus(final Transaction transaction) {
    return transaction.getTransactionStatus();
  }

  private static boolean isSuccessTransaction(final TransactionStatus senderTransactionStatus, final TransactionStatus receiverTransactionStatus) {
    return TransactionStatus.NORMAL.equals(senderTransactionStatus)
            && TransactionStatus.NORMAL.equals(receiverTransactionStatus);
  }
}
