package com.hello.money.v1.service;

import com.hello.money.config.redis.DistributedLock;
import com.hello.money.config.redis.DistributedMultiLock;
import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoneyDistributedLockService {

  private final WalletPort walletPort;
  private final MoneyTransactionService transactionService;
  private final EntityManager entityManager;

  @DistributedLock(key = "#walletId")
  public Wallet chargeMoneyWithLock(final Long walletId, final Transaction transaction) {

    entityManager.clear();

    final Wallet wallet = walletPort.findWalletById(walletId);
    final Transaction successfulTransaction = transactionService.executeCharge(wallet, transaction);
    return successfulTransaction.getWallet();
  }

  @DistributedMultiLock(keys = {"#senderWalletId", "#receiverWalletId"})
  public Wallet sendMoneyWithLock(final Long senderWalletId, final Long receiverWalletId, final Transaction senderTransaction, final Transaction receiverTransaction) {

    entityManager.clear();

    final Wallet senderWallet = walletPort.findWalletById(senderWalletId);
    final Wallet receiverWallet = walletPort.findWalletById(receiverWalletId);
    final Transaction successfulTransaction = transactionService.executeSend(senderWallet, receiverWallet, senderTransaction, receiverTransaction);
    return successfulTransaction.getWallet();
  }
}
