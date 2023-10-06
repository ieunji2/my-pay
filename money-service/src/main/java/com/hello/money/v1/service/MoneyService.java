package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyService {

  private final WalletPort walletPort;

  private final TransactionPort transactionPort;

  private final ExchangeApi exchangeApi;

  @Transactional
  public WalletResponse createWallet(final Long accountId) {

    checkWalletExists(accountId);

    final Wallet wallet = new Wallet(accountId);
    return WalletResponse.from(walletPort.saveWallet(wallet));
  }

  private void checkWalletExists(final Long accountId) {
    if (walletPort.existsWalletByAccountId(accountId)) {
      throw new IllegalArgumentException("해당 계정에 대한 지갑이 이미 존재합니다.");
    }
  }

  public WalletResponse getWallet(final Long accountId) {
    return WalletResponse.from(walletPort.findWalletByAccountId(accountId));
  }

  @Transactional
  public WalletResponse addMoney(final Long accountId, final AddMoneyRequest request) {

    final Wallet wallet = getWallet(accountId, request);
    final Transaction transaction = getTransaction(wallet, request);

    executeUpdate(wallet, transaction);

    return WalletResponse.from(walletPort.findWalletByAccountId(accountId));
  }

  private Wallet getWallet(final Long accountId, final AddMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    wallet.addMoney(request.amount());
    return wallet;
  }

  private Transaction getTransaction(final Wallet wallet, final AddMoneyRequest request) {
    final Transaction transaction = new Transaction(wallet, request.amount(), request.summary());
    return transactionPort.saveTransaction(transaction);
  }

  @Transactional
  public WalletResponse sendMoney(final Long accountId, final SendMoneyRequest request) {

    checkBalance(accountId, request);

    checkReceiver(request);

    final Wallet senderWallet = getWallet(accountId, request);
    final Transaction senderTransaction = getTransaction(senderWallet, request, "출금");

    executeUpdate(senderWallet, senderTransaction);

    final Wallet receiverWallet = getWallet(request);
    final Transaction receiverTransaction = getTransaction(receiverWallet, request, "입금");

    executeUpdate(receiverWallet, receiverTransaction);

    return WalletResponse.from(walletPort.findWalletByAccountId(accountId));
  }

  private void checkBalance(final Long accountId, final SendMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);

    if (request.amount().compareTo(wallet.getBalance()) > 0) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }
  }

  private void checkReceiver(final SendMoneyRequest request) {
    if (!walletPort.existsWalletById(request.receiverWalletId())) {
      new IllegalArgumentException("수취인의 지갑 ID가 존재하지 않습니다.");
    }

    final Wallet wallet = walletPort.findWalletById(request.receiverWalletId());

    final AccountResponse receiver = getReceiverAccount(wallet.getAccountId());
    if (receiver == null || !receiver.isValid()) {
      throw new IllegalArgumentException("수취인의 계정으로 송금할 수 없습니다.");
    }
  }

  private AccountResponse getReceiverAccount(final Long accountId) {
    AccountResponse response = null;
    try {
      response = exchangeApi.getAccount(accountId);
    } catch (RuntimeException e) {
      log.error(e.toString());
      //TODO error handle
    }
    return response;
  }

  private Wallet getWallet(final Long accountId, final SendMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletByAccountId(accountId);
    wallet.addMoney(request.amount().negate());
    return wallet;
  }

  private Wallet getWallet(final SendMoneyRequest request) {
    final Wallet wallet = walletPort.findWalletById(request.receiverWalletId());
    wallet.addMoney(request.amount());
    return wallet;
  }

  private Transaction getTransaction(final Wallet wallet, final SendMoneyRequest request, final String transactionType) {
    final Transaction transaction = new Transaction(
            wallet,
            request.receiverWalletId(),
            request.amount(),
            request.summary(),
            transactionType);
    return transactionPort.saveTransaction(transaction);
  }

  private void executeUpdate(final Wallet wallet, final Transaction transaction) {
    try {
      walletPort.saveWallet(wallet);
      transaction.success();
    } catch (Exception e) {
      log.error(e.toString());
      transaction.fail();
      throw new RuntimeException("update failed...");
    } finally {
      transactionPort.saveTransaction(transaction);
    }
  }
}
