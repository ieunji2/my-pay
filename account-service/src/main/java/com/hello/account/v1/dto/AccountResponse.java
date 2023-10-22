package com.hello.account.v1.dto;

import com.hello.account.domain.Account;
import org.springframework.util.Assert;

public record AccountResponse(Long id, String name, String email, boolean isValid) {
  public AccountResponse {
    Assert.notNull(id, "계정 ID는 필수입니다.");
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
  }

  public AccountResponse(final Account account) {
    this(
            account.getId(),
            account.getName(),
            account.getEmail(),
            account.isValid());
  }

  public static AccountResponse from(final Account account) {
    return new AccountResponse(account);
  }
}
