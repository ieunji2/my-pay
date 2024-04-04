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

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyDistributedLockService {

  private final MoneyTransactionService transactionService;
  private final WalletPort walletPort;
  private final ExchangeApi exchangeApi;

  @DistributedLock(key = "#walletId")
  public Wallet chargeMoneyWithLock(final ChargeMoneyServiceDto dto, final Long walletId) {
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    log.info("{}:{} - 충전 시작", Thread.currentThread().getId(), wallet.getBalance());
    wallet.addMoney(dto.amount());
    final Transaction transaction = transactionService.getSavedTransaction(wallet, dto);
    try {
      final Wallet savedWallet = transactionService.executeCharge(wallet, transaction);
      log.info("{}:{} - 충전 완료", Thread.currentThread().getId(), savedWallet.getBalance());
      return savedWallet;
//      return transactionService.executeCharge(wallet, transaction);
    } catch (Exception e) {
      transactionService.saveFailedTransaction(transaction);
      throw new ChargeTransactionFailedException();
    }
  }

  @DistributedMultiLock(keys = {"#walletId", "#dto.receiverWalletId()"})
  public Wallet sendMoneyWithLock(final SendMoneyServiceDto dto, final Long walletId) {
    final Wallet senderWallet = getSenderWallet(dto);
    log.info("{}:{} - 송금 시작", Thread.currentThread().getId(), senderWallet.getBalance());
    senderWallet.subtractMoney(dto.amount());
    final Transaction senderTransaction = transactionService.getSavedTransaction(senderWallet, dto);

    final Wallet receiverWallet = getReceiverWallet(dto);
    receiverWallet.addMoney(dto.amount());
    final Transaction receiverTransaction = transactionService.getSavedTransaction(receiverWallet, dto);

    try {
      final Wallet savedWallet = transactionService.executeSend(senderWallet, senderTransaction, receiverWallet, receiverTransaction);
      log.info("{}:{} - 송금 완료", Thread.currentThread().getId(), savedWallet.getBalance());
      return savedWallet;
//      return transactionService.executeSend(senderWallet, senderTransaction, receiverWallet, receiverTransaction);
    } catch (Exception e) {
      transactionService.saveFailedTransaction(senderTransaction, receiverTransaction);
      throw new SendTransactionFailedException();
    }
  }

  private Wallet getSenderWallet(final SendMoneyServiceDto dto) {
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    if (dto.amount().compareTo(wallet.getBalance()) > 0) {
      throw new InsufficientBalanceException();
    }
    return wallet;
  }

  private Wallet getReceiverWallet(final SendMoneyServiceDto dto) {
    final Wallet wallet = walletPort.findWalletById(dto.receiverWalletId());
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
