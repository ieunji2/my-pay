package com.hello.money.common.exception;

public class ChargeTransactionFailedException extends BusinessException {

  public ChargeTransactionFailedException(final String message) {
    super(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }

  public ChargeTransactionFailedException() {
    super(ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
