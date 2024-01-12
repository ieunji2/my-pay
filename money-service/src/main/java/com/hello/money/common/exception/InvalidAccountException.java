package com.hello.money.common.exception;

public class InvalidAccountException extends BusinessException {

  public InvalidAccountException(final String message) {
    super(message, ErrorCode.INVALID_ACCOUNT);
  }

  public InvalidAccountException() {
    super(ErrorCode.INVALID_ACCOUNT);
  }
}
