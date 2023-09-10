package com.hello.account.v1.controller;

import com.hello.account.v1.dto.AccountResponse;
import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import com.hello.account.v1.service.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(final AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  public AccountResponse registerAccount(@RequestBody final RegisterAccountRequest request) {
    return accountService.registerAccount(request);
  }

  @GetMapping("/{accountId}")
  public AccountResponse findAccount(@PathVariable final Long accountId) {
    return accountService.findAccount(accountId);
  }

  @PutMapping("/{accountId}")
  public AccountResponse modifyAccount(@PathVariable final Long accountId, @RequestBody ModifyAccountRequest request) {
    return accountService.modifyAccount(accountId, request);
  }

  @DeleteMapping("{accountId}")
  public AccountResponse removeAccount(@PathVariable final Long accountId) {
    return accountService.removeAccount(accountId);
  }
}
