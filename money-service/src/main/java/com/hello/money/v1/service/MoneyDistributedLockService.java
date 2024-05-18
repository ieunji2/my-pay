package com.hello.money.v1.service;

import com.hello.money.common.exception.ChargeTransactionFailedException;
import com.hello.money.common.exception.InsufficientBalanceException;
import com.hello.money.common.exception.InvalidAccountException;
import com.hello.money.common.exception.SendTransactionFailedException;
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

import java.math.BigInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyDistributedLockService {

  private final WalletPort walletPort;
  private final MoneyTransactionService transactionService;
  private final ExchangeApi exchangeApi;

  @DistributedLock(key = "#walletId")
  public Wallet chargeMoneyWithLock(final ChargeMoneyServiceDto dto, final Long walletId) {

    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    final Transaction requestedTransaction = transactionService.getRequestedTransaction(wallet, dto);
    try {
      final Transaction successfulTransaction = transactionService.executeCharge(wallet, requestedTransaction, dto.amount());
      return successfulTransaction.getWallet();
    } catch (Exception e) {
      final Transaction failedTransaction = transactionService.getFailedTransaction(requestedTransaction);
      throw new ChargeTransactionFailedException();
    }
  }

  @DistributedMultiLock(keys = {"#walletId", "#dto.receiverWalletId()"})
  public Wallet sendMoneyWithLock(final SendMoneyServiceDto dto, final Long walletId) {

    final Wallet senderWallet = getSenderWallet(dto.accountId(), dto.amount());
    final Transaction senderRequestedTransaction = transactionService.getRequestedTransaction(senderWallet, dto);

    final Wallet receiverWallet = getReceiverWallet(dto.receiverWalletId());
    final Transaction receiverRequestedTransaction = transactionService.getRequestedTransaction(receiverWallet, dto);
    try {
      final Transaction successfulTransaction = transactionService.executeSend(senderWallet, senderRequestedTransaction, receiverWallet, receiverRequestedTransaction, dto.amount());
      return successfulTransaction.getWallet();
    } catch (Exception e) {
      final Transaction senderFailedTransaction = transactionService.getFailedTransaction(senderRequestedTransaction);
      final Transaction receiverFailedTransaction = transactionService.getFailedTransaction(receiverRequestedTransaction);
      throw new SendTransactionFailedException();
    }
  }

  private Wallet getSenderWallet(final Long accountId, final BigInteger amount) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    if (amount.compareTo(wallet.getBalance()) > 0) {
      throw new InsufficientBalanceException();
    }
    return wallet;
  }

  private Wallet getReceiverWallet(final Long walletId) {
    final Wallet wallet = walletPort.findWalletById(walletId);
    final AccountResponse receiver = getReceiver(wallet.getAccountId());
    if (receiver == null || !receiver.isValid()) {
      throw new InvalidAccountException("수취인의 계정으로 송금할 수 없습니다.");
    }
    return wallet;
  }

  private AccountResponse getReceiver(final Long accountId) {
    return exchangeApi.getAccount(accountId);
  }
}
