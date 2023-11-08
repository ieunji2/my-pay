package com.hello.money.v1.controller;

import com.hello.money.domain.Wallet;
import com.hello.money.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MoneyMapper {

  @Mapping(source = "account.id", target = "accountId")
  @Mapping(source = "account.name", target = "accountName")
  AccountDto toAccountDto(Account account);

  @Mapping(source = "account.id", target = "accountId")
  @Mapping(source = "account.name", target = "accountName")
  ChargeMoneyServiceDto toChargeMoneyServiceDto(Account account, ChargeMoneyRequest request);

  @Mapping(source = "account.id", target = "accountId")
  @Mapping(source = "account.name", target = "accountName")
  SendMoneyServiceDto toSendMoneyServiceDto(Account account, SendMoneyRequest request);

  WalletResponse toWalletResponse(Wallet wallet);
}
