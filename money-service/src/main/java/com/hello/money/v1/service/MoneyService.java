package com.hello.money.v1.service;

import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;

public interface MoneyService {

  WalletResponse createWallet(final Account account);

  WalletResponse getWallet(final Account account);

  WalletResponse chargeMoney(final Account account, final ChargeMoneyRequest request);

  WalletResponse sendMoney(final Account account, final SendMoneyRequest request);
}
