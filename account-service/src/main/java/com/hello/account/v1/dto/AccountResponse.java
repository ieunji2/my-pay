package com.hello.account.v1.dto;

import com.hello.account.domain.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountResponse(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank String email,
        boolean isValid) {

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
