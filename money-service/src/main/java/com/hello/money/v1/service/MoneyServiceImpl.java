package com.hello.money.v1.service;

import com.hello.money.common.exception.*;
import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyServiceImpl implements MoneyService {

  private final WalletPort walletPort;
  private final MoneyDistributedLockService distributedLockService;
  private final MoneyTransactionService transactionService;
  private final ExchangeApi exchangeApi;

  @Override
  public Wallet createWallet(final CreateWalletServiceDto dto) {
    if (isExistsWallet(dto.accountId())) {
      throw new WalletAlreadyExistsException("해당 계정에 대한 지갑이 이미 존재합니다.");
    }
    return walletPort.saveWallet(new Wallet(dto.accountId()));
  }

  @Override
  public Wallet getWallet(final GetWalletServiceDto dto) {
    return walletPort.findWalletByAccountId(dto.accountId());
  }

  @Override
  public Wallet chargeMoney(final ChargeMoneyServiceDto dto) {

    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    final Transaction requestedTransaction = transactionService.getRequestedTransaction(wallet, dto);
    try {
      return distributedLockService.chargeMoneyWithLock(wallet.getId(), requestedTransaction);
    } catch (Exception e) {
      final Transaction failedTransaction = transactionService.getFailedTransaction(requestedTransaction);
      throw new ChargeTransactionFailedException(e.getMessage());
    }
  }

  @Override
  public Wallet sendMoney(final SendMoneyServiceDto dto) {

    final Wallet senderWallet = getSenderWallet(dto.accountId(), dto.amount());
    final Wallet receiverWallet = getReceiverWallet(dto.receiverWalletId());

    final Transaction senderRequestedTransaction = transactionService.getRequestedTransaction(senderWallet, dto);
    final Transaction receiverRequestedTransaction = transactionService.getRequestedTransaction(receiverWallet, dto);
    try {
      return distributedLockService.sendMoneyWithLock(senderWallet.getId(), receiverWallet.getId(), senderRequestedTransaction, receiverRequestedTransaction);
    } catch (Exception e) {
      final Transaction senderFailedTransaction = transactionService.getFailedTransaction(senderRequestedTransaction);
      final Transaction receiverFailedTransaction = transactionService.getFailedTransaction(receiverRequestedTransaction);
      throw new SendTransactionFailedException(e.getMessage());
    }
  }

  private boolean isExistsWallet(final Long accountId) {
    return walletPort.existsWalletByAccountId(accountId);
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
