package com.hello.money.v1.controller;

import com.hello.money.config.Authenticated;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/moneys")
public class MoneyController {

  private final MoneyService moneyService;

  @PostMapping
  public WalletResponse createWallet(@Authenticated final Account account) {
    return moneyService.createWallet(account);
  }

  @GetMapping
  public WalletResponse getWallet(@Authenticated final Account account) {
    return moneyService.getWallet(account);
  }

  @PostMapping("/charge")
  public WalletResponse addMoney(@Authenticated final Account account, @RequestBody final AddMoneyRequest request) {
    return moneyService.addMoney(account, request);
  }

  @PostMapping("/send")
  public WalletResponse sendMoney(@Authenticated final Account account, @RequestBody final SendMoneyRequest request) {
    return moneyService.sendMoney(account, request);
  }
}
