package com.hello.money.v1.service;

import com.hello.money.v1.dto.AccountResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface ExchangeApi {

  @GetExchange("/v1/accounts/{accountId}")
  AccountResponse getAccount(@PathVariable Long accountId);
}
