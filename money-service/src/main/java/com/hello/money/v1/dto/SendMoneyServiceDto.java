package com.hello.money.v1.dto;

import com.hello.money.common.validator.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigInteger;

public record SendMoneyServiceDto(
        @NotNull Long accountId,
        @NotBlank String accountName,
        @NotNull Long receiverWalletId,
        @Positive BigInteger amount,
        String summary) {

  public SendMoneyServiceDto(
          @NotNull final Long accountId,
          @NotBlank final String accountName,
          @NotNull final Long receiverWalletId,
          @Positive final BigInteger amount,
          final String summary) {
    this.accountId = accountId;
    this.accountName = accountName;
    this.receiverWalletId = receiverWalletId;
    this.amount = amount;
    this.summary = summary;
    SelfValidating.validateSelf(this);
  }
}
