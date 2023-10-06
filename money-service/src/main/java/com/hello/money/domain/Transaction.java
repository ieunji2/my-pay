package com.hello.money.domain;

import org.springframework.util.Assert;

import java.math.BigInteger;

public class Transaction {
  private Long id;
  private Wallet wallet;
  private Long receiverWalletId;
  private BigInteger amount;
  private String summary;
  private String transactionType;
  private String transactionStatus;

  public Transaction(
          final Wallet wallet,
          final BigInteger amount,
          final String summary) {
    Assert.notNull(wallet, "지갑은 필수입니다.");
    this.wallet = wallet;
    this.receiverWalletId = wallet.getId();
    this.amount = amount;
    this.summary = summary;
    this.transactionType = "입금";
    this.transactionStatus = "요청";
  }

  public Transaction(
          final Wallet wallet,
          final Long receiverWalletId,
          final BigInteger amount,
          final String summary,
          final String transactionType) {
    Assert.notNull(wallet, "지갑은 필수입니다.");
    Assert.notNull(receiverWalletId, "수취인 지갑 ID는 필수입니다.");
    Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
    Assert.hasText(transactionType, "거래 유형은 필수입니다.");
    this.wallet = wallet;
    this.receiverWalletId = receiverWalletId;
    this.amount = amount;
    this.summary = summary;
    this.transactionType = transactionType;
    this.transactionStatus = "요청";
  }

  public void success() {
    this.transactionStatus = "정상";
  }

  public void fail() {
    this.transactionStatus = "오류";
  }

  public void assignId(final Long id) {
    this.id = id;
  }

  public Long getId() {
    return this.id;
  }
}
