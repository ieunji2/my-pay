package com.hello.money.v1.controller;

import com.hello.money.config.auth.Authenticated;
import com.hello.money.v1.dto.*;
import com.hello.money.v1.controller.mapper.MoneyMapper;
import com.hello.money.v1.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/money")
public class MoneyController {

  private final MoneyService moneyService;
  private final MoneyMapper mapper;

  @PostMapping
  public WalletResponse createWallet(@Authenticated final Account account) {
    final CreateWalletServiceDto dto = mapper.toCreateWalletServiceDto(account);
    return mapper.toWalletResponse(moneyService.createWallet(dto));
  }

  @GetMapping
  public WalletResponse getWallet(@Authenticated final Account account) {
    final GetWalletServiceDto dto = mapper.toGetWalletServiceDto(account);
    return mapper.toWalletResponse(moneyService.getWallet(dto));
  }

  @PostMapping("/charge")
  public WalletResponse chargeMoney(@Authenticated final Account account, @RequestBody final ChargeMoneyRequest request) {
    final ChargeMoneyServiceDto dto = mapper.toChargeMoneyServiceDto(account, request);
    return mapper.toWalletResponse(moneyService.chargeMoney(dto));
  }

  @PostMapping("/send")
  public WalletResponse sendMoney(@Authenticated final Account account, @RequestBody final SendMoneyRequest request) {
    final SendMoneyServiceDto sendMoneyServiceDto = mapper.toSendMoneyServiceDto(account, request);
    return mapper.toWalletResponse(moneyService.sendMoney(sendMoneyServiceDto));
  }
}
