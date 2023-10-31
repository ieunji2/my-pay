package com.hello.money.v1.service;

import com.hello.money.domain.Transaction;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.*;
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

  private final MoneyTransactionService moneyTransactionService;

  @Transactional
  public WalletResponse createWallet(final Account account) {

    checkWalletExists(account.id());

    final Wallet wallet = walletPort.saveWallet(new Wallet(account.id()));

    return WalletResponse.from(wallet);
  }

  public WalletResponse getWallet(final Account account) {

    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    return WalletResponse.from(wallet);
  }

  public WalletResponse saveMoney(final Account account, final SaveMoneyRequest request) {

    //1. 내 지갑 찾기
    final Wallet wallet = walletPort.findWalletByAccountId(account.id());

    //2. addMoney
    wallet.addMoney(request.amount());

    //3. 트랜잭션 요청 내역 쌓기
    final Transaction transaction = transactionPort.saveTransaction(new Transaction(
            wallet,
            wallet.getId(),
            request.amount(),
            request.summary(),
            "입금"));

    //4. 지갑 업데이트
    try {
      moneyTransactionService.executeSave(wallet);
      transaction.success();
    } catch (Exception e) {
      log.error(e.toString());
      transaction.fail();
      throw new RuntimeException("Save failed");
    } finally {
      transactionPort.saveTransaction(transaction);
    }

    //6. 결과 리턴
    return getWallet(account);
  }

  public WalletResponse sendMoney(final Account account, final SendMoneyRequest request) {

    //1. sender 잔액 확인 및 지갑 찾기
    if (!walletPort.existsWalletByAccountId(account.id())) {
      throw new IllegalArgumentException("송금인의 지갑이 존재하지 않습니다.");
    }

    final Wallet senderWallet = walletPort.findWalletByAccountId(account.id());

    if (request.amount().compareTo(senderWallet.getBalance()) > 0) {
      throw new IllegalArgumentException("잔액이 부족합니다.");
    }

    //2. receiver 확인 및 지갑 찾기
    if (!walletPort.existsWalletById(request.receiverWalletId())) {
      throw new IllegalArgumentException("수취인의 지갑이 존재하지 않습니다.");
    }

    final Wallet receiverWallet = walletPort.findWalletById(request.receiverWalletId());

    final AccountResponse receiver = getReceiver(receiverWallet.getAccountId());
    if (receiver == null || !receiver.isValid()) {
      throw new IllegalArgumentException("수취인의 계정으로 송금할 수 없습니다.");
    }

    //3. sender subtractMoney
    senderWallet.subtractMoney(request.amount());

    //4. sender 트랜잭션 요청 내역 쌓기
    final Transaction senderTransaction = transactionPort.saveTransaction(new Transaction(
            senderWallet,
            request.receiverWalletId(),
            request.amount(),
            request.summary(),
            "출금"));

    //5. receiver addMoney
    receiverWallet.addMoney(request.amount());

    //6. receiver 트랙잭션 요청 내역 쌓기
    final Transaction receiverTransaction = transactionPort.saveTransaction(new Transaction(
            receiverWallet,
            receiverWallet.getId(),
            request.amount(),
            request.summary(),
            "입금"));

    //7. 지갑 업데이트
    try {
      moneyTransactionService.executeSend(senderWallet, receiverWallet);
      senderTransaction.success();
      receiverTransaction.success();
    } catch (Exception e) {
      senderTransaction.fail();
      receiverTransaction.fail();
      throw new RuntimeException("Send failed");
    } finally {
      //8. 트랜잭션 성공/실패 내역 쌓기
      transactionPort.saveTransaction(senderTransaction);
      transactionPort.saveTransaction(receiverTransaction);
    }

    //9. 결과 리턴
    return getWallet(account);
  }

  private void checkWalletExists(final Long accountId) {
    if (walletPort.existsWalletByAccountId(accountId)) {
      throw new IllegalArgumentException("해당 계정에 대한 지갑이 이미 존재합니다.");
    }
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
}
