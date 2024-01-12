package com.hello.account.v1.controller;

import com.hello.account.v1.dto.AccountResponse;
import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import com.hello.account.v1.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  public AccountResponse registerAccount(
          @RequestBody @Valid final RegisterAccountRequest request) {
    return accountService.registerAccount(request);
  }

  @GetMapping("/{accountId}")
  public AccountResponse findAccount(
          @PathVariable @NotNull final Long accountId) {
    return accountService.findAccount(accountId);
  }

  @PutMapping("/{accountId}")
  public AccountResponse modifyAccount(
          @PathVariable @NotNull final Long accountId,
          @RequestBody @Valid ModifyAccountRequest request) {
    return accountService.modifyAccount(accountId, request);
  }

  @DeleteMapping("{accountId}")
  public AccountResponse removeAccount(
          @PathVariable @NotNull final Long accountId) {
    return accountService.removeAccount(accountId);
  }
}
