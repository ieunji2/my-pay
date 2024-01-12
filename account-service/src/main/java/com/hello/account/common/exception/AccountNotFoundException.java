package com.hello.account.common.exception;

public class AccountNotFoundException extends BusinessException {

  public AccountNotFoundException(final String message) {
    super(message, ErrorCode.ACCOUNT_NOT_FOUND);
  }

  public AccountNotFoundException() {
    super(ErrorCode.ACCOUNT_NOT_FOUND);
  }
}
