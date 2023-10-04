package com.hello.money.domain;

import org.springframework.util.Assert;

import java.math.BigInteger;

public class Wallet {
  private Long id;
  private final Long accountId;
  private BigInteger balance;

  public Wallet(final Long accountId) {
    Assert.notNull(accountId, "계정 ID는 필수입니다.");
    this.id = 1L;
    this.accountId = accountId;
    this.balance = BigInteger.ZERO;
  }

  public Long getId() {
    return id;
  }

  public Long getAccountId() {
    return accountId;
  }

  public BigInteger getBalance() {
    return balance;
  }

  public void assignId(final Long id) {
    this.id = id;
  }

  public void addMoney(final BigInteger amount) {
    this.balance = this.balance.add(amount);
  }
}
