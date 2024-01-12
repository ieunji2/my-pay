package com.hello.money.common.exception;

public class InsufficientBalanceException extends BusinessException {

  public InsufficientBalanceException(final String message) {
    super(message, ErrorCode.INSUFFICIENT_BALANCE);
  }

  public InsufficientBalanceException() {
    super(ErrorCode.INSUFFICIENT_BALANCE);
  }
}
