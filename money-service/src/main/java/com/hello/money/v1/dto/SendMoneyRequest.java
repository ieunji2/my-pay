package com.hello.money.v1.dto;

import org.springframework.util.Assert;

import java.math.BigInteger;

public record SendMoneyRequest(Long receiverWalletId, BigInteger amount, String summary) {
  public SendMoneyRequest {
    Assert.notNull(receiverWalletId, "수취인 지갑 ID는 필수입니다.");
    Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
  }
}
