package com.hello.money.domain;

import lombok.Getter;

@Getter
public enum TransactionStatus {
  REQUEST("요청"),
  NORMAL("정상"),
  ERROR("오류")
  ;

  private final String value;
  TransactionStatus(final String value) {
    this.value = value;
  }
}
