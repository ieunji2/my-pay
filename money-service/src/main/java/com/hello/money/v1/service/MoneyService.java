package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.CreateWalletServiceDto;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.GetWalletServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;

public interface MoneyService {

  Wallet createWallet(final CreateWalletServiceDto dto);

  Wallet getWallet(final GetWalletServiceDto dto);

  Wallet chargeMoney(final ChargeMoneyServiceDto dto);

  Wallet sendMoney(final SendMoneyServiceDto dto);
}
