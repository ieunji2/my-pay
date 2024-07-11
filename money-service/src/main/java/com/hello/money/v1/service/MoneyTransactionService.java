package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@RequiredArgsConstructor
@Service
public class MoneyTransactionService {

  private final TransactionPort transactionPort;
  private final WalletPort walletPort;

  public Transaction getRequestedTransaction(final Wallet wallet, final ChargeMoneyServiceDto dto) {
    return transactionPort.saveTransaction(
            new Transaction(
                    wallet,
                    wallet.getId(),
                    dto.amount(),
                    dto.summary()));
  }

  public Transaction getRequestedTransaction(final Wallet wallet, final SendMoneyServiceDto dto) {
    return transactionPort.saveTransaction(
            new Transaction(
                    wallet,
                    dto.receiverWalletId(),
                    dto.amount(),
                    dto.summary()));
  }

  public Transaction getFailedTransaction(final Transaction transaction) {
    return transactionPort.saveTransaction(transaction.failed());
  }

  @Transactional
  public Transaction executeCharge(final Wallet wallet, final Transaction transaction) {
    return executeTransaction(wallet, transaction, Wallet::addMoney);
  }

  @Transactional
  public Transaction executeSend(
          final Wallet senderWallet,
          final Wallet receiverWallet,
          final Transaction senderTransaction,
          final Transaction receiverTransaction) {

    final Transaction executedReceiverTransaction = executeTransaction(receiverWallet, receiverTransaction, Wallet::addMoney);
    final Transaction executedSenderTransaction = executeTransaction(senderWallet, senderTransaction, Wallet::subtractMoney);

    return executedSenderTransaction;
  }

  private Transaction executeTransaction(final Wallet wallet, final Transaction transaction, final ChangeWalletBalance change) {
    final BigInteger amount = transaction.getAmount();
    walletPort.saveWallet(change.apply(wallet, amount));
    return transactionPort.saveTransaction(transaction.succeed());
  }
}
