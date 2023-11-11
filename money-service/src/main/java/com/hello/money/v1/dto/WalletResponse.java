package com.hello.money.v1.dto;

import com.hello.money.common.validator.SelfValidating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;

public record WalletResponse(
        @NotNull Long id,
        @NotNull Long accountId,
        @Min(0) BigInteger balance) {

  public WalletResponse(
          @NotNull final Long id,
          @NotNull final Long accountId,
          @Min(0) final BigInteger balance) {
    this.id = id;
    this.accountId = accountId;
    this.balance = balance;
    SelfValidating.validateSelf(this);
  }
}
