package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.TransactionStatus;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MoneyTransactionService {

  private final WalletPort walletPort;
  private final TransactionPort transactionPort;

  public Transaction getSavedTransaction(final Wallet wallet, final ChargeMoneyServiceDto dto) {
    return transactionPort.saveTransaction(
            new Transaction(
                    wallet,
                    wallet.getId(),
                    dto.amount(),
                    dto.summary()));
  }

  public Transaction getSavedTransaction(final Wallet wallet, final SendMoneyServiceDto dto) {
    return transactionPort.saveTransaction(
            new Transaction(
                    wallet,
                    dto.receiverWalletId(),
                    dto.amount(),
                    dto.summary()));
  }

  @Transactional
  public Wallet executeCharge(final Wallet wallet, final Transaction transaction) {
    final Wallet savedWallet = walletPort.saveWallet(wallet);
    transactionPort.saveTransaction(transaction.success());
    return savedWallet;
  }

  @Transactional
  public Wallet executeSend(
          final Wallet senderWallet,
          final Transaction senderTransaction,
          final Wallet receiverWallet,
          final Transaction receiverTransaction) {

    final Wallet savedWallet = walletPort.saveWallet(senderWallet);
    transactionPort.saveTransaction(senderTransaction.success());

    walletPort.saveWallet(receiverWallet);
    transactionPort.saveTransaction(receiverTransaction.success());

    return savedWallet;
  }

  public TransactionStatus saveFailedTransaction(final Transaction transaction) {
    return transactionPort.saveTransaction(transaction.fail())
                          .getTransactionStatus();
  }

  @Transactional
  public TransactionStatus saveFailedTransaction(final Transaction senderTransaction, final Transaction receiverTransaction) {
    transactionPort.saveTransaction(senderTransaction.fail());
    transactionPort.saveTransaction(receiverTransaction.fail());
    return TransactionStatus.ERROR;
  }
}
