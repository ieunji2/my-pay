package com.hello.money.v1.service;

import com.hello.money.config.redis.DistributedLock;
import com.hello.money.config.redis.DistributedMultiLock;
import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyDistributedLockService {

  private final MoneyTransactionService moneyTransactionService;
  private final WalletPort walletPort;
  private final TransactionPort transactionPort;
  private final ExchangeApi exchangeApi;

  @DistributedLock(key = "#walletId")
  public void chargeMoneyWithLock(final Account account, final ChargeMoneyRequest request, final Long walletId) {
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());
    wallet.addMoney(request.amount());
    final Transaction transaction = getSavedTransaction(wallet, request);
    try {
      moneyTransactionService.executeCharge(wallet);
      transaction.success();
    } catch (Exception e) {
      transaction.fail();
      throw new RuntimeException("Failed to charge money");
    } finally {
      transactionPort.saveTransaction(transaction);
    }
  }

  private Transaction getSavedTransaction(final Wallet wallet, final ChargeMoneyRequest request) {
    return transactionPort.saveTransaction(new Transaction(
            wallet,
            wallet.getId(),
            request.amount(),
            request.summary()));
  }

  @DistributedMultiLock(keys = {"#walletId", "#request.receiverWalletId()"})
  public void sendMoneyWithLock(final Account account, final SendMoneyRequest request, final Long walletId) {

    final Wallet senderWallet = getSenderWallet(account, request);

    final Wallet receiverWallet = getReceiverWallet(request);

    senderWallet.subtractMoney(request.amount());
    final Transaction senderTransaction = getSavedTransaction(senderWallet, request);

    receiverWallet.addMoney(request.amount());
    final Transaction receiverTransaction = getSavedTransaction(receiverWallet, request);

    try {
      moneyTransactionService.executeSend(senderWallet, receiverWallet);
      senderTransaction.success();
      receiverTransaction.success();
    } catch (Exception e) {
      senderTransaction.fail();
      receiverTransaction.fail();
      throw new RuntimeException("Failed to send money");
    } finally {
      transactionPort.saveTransaction(senderTransaction);
      transactionPort.saveTransaction(receiverTransaction);
    }
  }

  private Wallet getSenderWallet(final Account account, final SendMoneyRequest request) {
    final Wallet senderWallet = walletPort.findWalletByAccountId(account.id());
    if (request.amount().compareTo(senderWallet.getBalance()) > 0) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }
    return senderWallet;
  }

  private Wallet getReceiverWallet(final SendMoneyRequest request) {
    final Wallet receiverWallet = walletPort.findWalletById(request.receiverWalletId());
    final AccountResponse receiver = getReceiver(receiverWallet.getAccountId());
    if (receiver == null || !receiver.isValid()) {
      throw new IllegalArgumentException("수취인의 계정으로 송금할 수 없습니다.");
    }
    return receiverWallet;
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

  private Transaction getSavedTransaction(final Wallet wallet, final SendMoneyRequest request) {
    return transactionPort.saveTransaction(new Transaction(
            wallet,
            request.receiverWalletId(),
            request.amount(),
            request.summary()));
  }
}
