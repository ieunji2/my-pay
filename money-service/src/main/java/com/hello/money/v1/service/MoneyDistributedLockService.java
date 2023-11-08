package com.hello.money.v1.service;

import com.hello.money.config.redis.DistributedLock;
import com.hello.money.config.redis.DistributedMultiLock;
import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyDistributedLockService {

  private final MoneyTransactionService transactionService;
  private final WalletPort walletPort;
  private final TransactionPort transactionPort;
  private final ExchangeApi exchangeApi;

  @DistributedLock(key = "#walletId")
  public Wallet chargeMoneyWithLock(final ChargeMoneyServiceDto dto, final Long walletId) {
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    wallet.addMoney(dto.amount());
    final Transaction transaction = getSavedTransaction(wallet, dto);
    try {
      transactionService.executeCharge(wallet, transaction);
    } catch (RuntimeException e) {
      transaction.fail();
      transactionPort.saveTransaction(transaction);
      throw new RuntimeException("Failed to charge money", e);
    }
    return walletPort.findWalletById(walletId);
  }

  private Transaction getSavedTransaction(final Wallet wallet, final ChargeMoneyServiceDto dto) {
    return transactionPort.saveTransaction(new Transaction(
            wallet,
            wallet.getId(),
            dto.amount(),
            dto.summary()));
  }

  @DistributedMultiLock(keys = {"#walletId", "#dto.receiverWalletId()"})
  public Wallet sendMoneyWithLock(final SendMoneyServiceDto dto, final Long walletId) {

    final Wallet senderWallet = getSenderWallet(dto);
    senderWallet.subtractMoney(dto.amount());
    final Transaction senderTransaction = getSavedTransaction(senderWallet, dto);

    final Wallet receiverWallet = getReceiverWallet(dto);
    receiverWallet.addMoney(dto.amount());
    final Transaction receiverTransaction = getSavedTransaction(receiverWallet, dto);

    try {
      transactionService.executeSend(senderWallet, senderTransaction, receiverWallet, receiverTransaction);
    } catch (RuntimeException e) {
      senderTransaction.fail();
      transactionPort.saveTransaction(senderTransaction);
      receiverTransaction.fail();
      transactionPort.saveTransaction(receiverTransaction);
      throw new RuntimeException("Failed to send money", e);
    }
    return walletPort.findWalletById(walletId);
  }

  private Wallet getSenderWallet(final SendMoneyServiceDto dto) {
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    if (dto.amount().compareTo(wallet.getBalance()) > 0) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }
    return wallet;
  }

  private Wallet getReceiverWallet(final SendMoneyServiceDto dto) {
    final Wallet wallet = walletPort.findWalletById(dto.receiverWalletId());
    final AccountResponse receiver = getReceiver(wallet.getAccountId());
    if (receiver == null || !receiver.isValid()) {
      throw new IllegalArgumentException("수취인의 계정으로 송금할 수 없습니다.");
    }
    return wallet;
  }

  private AccountResponse getReceiver(final Long accountId) {
    AccountResponse response = null;
    try {
      response = exchangeApi.getAccount(accountId);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return response;
  }

  private Transaction getSavedTransaction(final Wallet wallet, final SendMoneyServiceDto dto) {
    return transactionPort.saveTransaction(new Transaction(
            wallet,
            dto.receiverWalletId(),
            dto.amount(),
            dto.summary()));
  }
}
