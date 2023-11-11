package com.hello.money.v1.dto;

import com.hello.money.common.validator.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GetWalletServiceDto(
        @NotNull Long accountId,
        @NotBlank String accountName) {

  public GetWalletServiceDto(
          @NotNull final Long accountId,
          @NotBlank final String accountName) {
    this.accountId = accountId;
    this.accountName = accountName;
    SelfValidating.validateSelf(this);
  }
}
