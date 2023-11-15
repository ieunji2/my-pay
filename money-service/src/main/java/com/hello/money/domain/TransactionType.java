package com.hello.money.domain;

import lombok.Getter;

@Getter
public enum TransactionType {
  DEPOSIT("입금"),
  WITHDRAW("출금")
  ;

  private final String value;
  TransactionType(final String value) {
    this.value = value;
  }
}
