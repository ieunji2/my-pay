package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MoneyTransactionService {

  private final WalletPort walletPort;

  @Transactional
  public void executeCharge(final Wallet wallet) {
    walletPort.saveWallet(wallet);
  }

  @Transactional
  public void executeSend(final Wallet senderWallet, final Wallet receiverWallet) {
    walletPort.saveWallet(senderWallet);
    walletPort.saveWallet(receiverWallet);
  }
}
