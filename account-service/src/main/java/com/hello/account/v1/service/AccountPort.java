package com.hello.account.v1.service;

import com.hello.account.domain.Account;

public interface AccountPort {
  Account saveAccount(final Account account);

  Account findAccountById(final Long accountId);

  void removeAccount(final Account account);
}
