package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoneyExceptionTest {

  private final WalletPort walletPort;

  public MoneyExceptionTest(final WalletPort walletPort) {
    this.walletPort = walletPort;
  }

  @Transactional
  public void executeCharge(final Wallet wallet) {
    walletPort.saveWallet(wallet);
    throw new RuntimeException("Rollback executeSave");
  }

  @Transactional
  public void executeSend(final Wallet senderWallet, final Wallet receiverWallet) {
    walletPort.saveWallet(senderWallet);
    walletPort.saveWallet(receiverWallet);
    throw new RuntimeException("Rollback executeSend");
  }
}