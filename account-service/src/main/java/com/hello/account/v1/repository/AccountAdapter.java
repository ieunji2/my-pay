package com.hello.account.v1.repository;

import com.hello.account.common.exception.AccountNotFoundException;
import com.hello.account.domain.Account;
import com.hello.account.v1.service.AccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class AccountAdapter implements AccountPort {

  private final AccountRepository accountRepository;

  @Override
  public Account saveAccount(final Account account) {
    return accountRepository.save(account);
  }

  @Override
  public Account findAccountById(final Long accountId) {
    return accountRepository.findById(accountId)
                            .orElseThrow(AccountNotFoundException::new);
  }

  @Override
  public void removeAccount(final Account account) {
    accountRepository.delete(account);
  }
}
