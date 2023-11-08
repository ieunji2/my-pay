package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.AccountDto;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;

public interface MoneyService {

  Wallet createWallet(final AccountDto dto);

  Wallet getWallet(final AccountDto dto);

  Wallet chargeMoney(final ChargeMoneyServiceDto dto);

  Wallet sendMoney(final SendMoneyServiceDto dto);
}
