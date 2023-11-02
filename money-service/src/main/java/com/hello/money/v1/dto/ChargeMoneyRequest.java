package com.hello.money.v1.dto;

import org.springframework.util.Assert;

import java.math.BigInteger;

public record ChargeMoneyRequest(BigInteger amount, String summary) {
  public ChargeMoneyRequest {
    Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
  }
}
