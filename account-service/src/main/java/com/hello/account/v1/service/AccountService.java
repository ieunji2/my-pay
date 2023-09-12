package com.hello.account.v1.service;

import com.hello.account.domain.Account;
import com.hello.account.v1.dto.AccountResponse;
import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountService {

  private final AccountPort accountPort;

  @Transactional
  public AccountResponse registerAccount(final RegisterAccountRequest request) {
    final Account account = new Account(
            request.name(),
            request.email());
    return AccountResponse.from(accountPort.saveAccount(account));
  }

  public AccountResponse findAccount(final Long accountId) {
    return AccountResponse.from(accountPort.findAccountById(accountId));
  }

  @Transactional
  public AccountResponse modifyAccount(final Long accountId, final ModifyAccountRequest request) {
    final Account account = accountPort.findAccountById(accountId);
    account.modify(
            request.name(),
            request.email(),
            request.isValid());
    return AccountResponse.from(accountPort.saveAccount(account));
  }

  @Transactional
  public AccountResponse removeAccount(final Long accountId) {
    final Account account = accountPort.findAccountById(accountId);
    accountPort.removeAccount(account);
    return AccountResponse.from(account);
  }
}
