package com.hello.money.v1.controller;

import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/moneys")
public class MoneyController {

  private final MoneyService moneyService;

  @PostMapping
  public WalletResponse createWallet(@RequestHeader("x-account-id") final String xAccountId) {
    Assert.hasText(xAccountId, "잘못된 요청입니다.");
    final Long accountId = Long.valueOf(decode(xAccountId));
    return moneyService.createWallet(accountId);
  }

  @GetMapping
  public WalletResponse getWallet(@RequestHeader("x-account-id") final String xAccountId) {
    Assert.hasText(xAccountId, "잘못된 요청입니다.");
    final Long accountId = Long.valueOf(decode(xAccountId));
    return moneyService.getWallet(accountId);
  }

  @PostMapping("/charge")
  public WalletResponse addMoney(@RequestHeader("x-account-id") final String xAccountId, @RequestBody final AddMoneyRequest request) {
    Assert.hasText(xAccountId, "잘못된 요청입니다.");
    final Long accountId = Long.valueOf(decode(xAccountId));
    return moneyService.addMoney(accountId, request);
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }
}
