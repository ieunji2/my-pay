package com.hello.account.v1.service;

import com.hello.account.domain.Account;
import com.hello.account.v1.dto.AccountResponse;
import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

  private final AccountPort accountPort;

  public AccountService(final AccountPort accountPort) {
    this.accountPort = accountPort;
  }

  @Transactional
  public AccountResponse registerAccount(final RegisterAccountRequest request) {
    final Account account = new Account(request.name(), request.email());
    final Account savedAccount = accountPort.saveAccount(account);
    return new AccountResponse(
            savedAccount.getId(),
            savedAccount.getName(),
            savedAccount.getEmail(),
            savedAccount.isValid());
  }

  public AccountResponse findAccount(final Long accountId) {
    final Account account = accountPort.findAccountById(accountId);
    return new AccountResponse(
            account.getId(),
            account.getName(),
            account.getEmail(),
            account.isValid());
  }

  @Transactional
  public AccountResponse modifyAccount(final Long accountId, final ModifyAccountRequest request) {
    final Account account = accountPort.findAccountById(accountId);
    account.modify(request.name(), request.email(), request.isValid());
    final Account savedAccount = accountPort.saveAccount(account);
    return new AccountResponse(
            savedAccount.getId(),
            savedAccount.getName(),
            savedAccount.getEmail(),
            savedAccount.isValid());
  }

  @Transactional
  public AccountResponse removeAccount(final Long accountId) {
    final Account account = accountPort.findAccountById(accountId);
    accountPort.removeAccount(account);
    return new AccountResponse(
            account.getId(),
            account.getName(),
            account.getEmail(),
            account.isValid());
  }
}
