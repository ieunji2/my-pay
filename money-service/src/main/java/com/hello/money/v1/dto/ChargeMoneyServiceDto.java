package com.hello.money.v1.dto;

import com.hello.money.common.validator.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigInteger;

public record ChargeMoneyServiceDto(
        @NotNull Long accountId,
        @NotBlank String accountName,
        @Positive BigInteger amount,
        String summary) {

  public ChargeMoneyServiceDto(
          @NotNull final Long accountId,
          @NotBlank final String accountName,
          @Positive final BigInteger amount,
          final String summary) {
    this.accountId = accountId;
    this.accountName = accountName;
    this.amount = amount;
    this.summary = summary;
    SelfValidating.validateSelf(this);
  }
}
