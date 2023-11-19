package com.hello.money.v1.service;

import com.hello.money.common.exception.WalletAlreadyExistsException;
import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.ChargeMoneyServiceDto;
import com.hello.money.v1.dto.CreateWalletServiceDto;
import com.hello.money.v1.dto.GetWalletServiceDto;
import com.hello.money.v1.dto.SendMoneyServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MoneyServiceImpl implements MoneyService {

  private final MoneyDistributedLockService distributedLockService;
  private final WalletPort walletPort;

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
    return distributedLockService.chargeMoneyWithLock(dto, wallet.getId());
  }

  @Override
  public Wallet sendMoney(final SendMoneyServiceDto dto) {
    final Wallet wallet = walletPort.findWalletByAccountId(dto.accountId());
    return distributedLockService.sendMoneyWithLock(dto, wallet.getId());
  }

  private boolean isExistsWallet(final Long accountId) {
    return walletPort.existsWalletByAccountId(accountId);
  }
}
