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
    final BigInteger amount = transaction.getAmount();
    walletPort.saveWallet(wallet.addMoney(amount));
    return transactionPort.saveTransaction(transaction.succeed());
  }

  @Transactional
  public Transaction executeSend(
          final Wallet senderWallet,
          final Wallet receiverWallet,
          final Transaction senderTransaction,
          final Transaction receiverTransaction) {

    final BigInteger amount = senderTransaction.getAmount();

    walletPort.saveWallet(receiverWallet.addMoney(amount));
    transactionPort.saveTransaction(receiverTransaction.succeed());

    walletPort.saveWallet(senderWallet.subtractMoney(amount));
    return transactionPort.saveTransaction(senderTransaction.succeed());
  }
}
