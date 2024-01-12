package com.hello.money.common.exception;

public class SendTransactionFailedException extends BusinessException {

  public SendTransactionFailedException(final String message) {
    super(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }

  public SendTransactionFailedException() {
    super(ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
