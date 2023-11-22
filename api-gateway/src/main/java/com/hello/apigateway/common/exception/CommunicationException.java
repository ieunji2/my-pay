package com.hello.apigateway.common.exception;

public class CommunicationException extends BusinessException {

  public CommunicationException(final String message) {
    super(message, ErrorCode.COMMUNICATION_ERROR);
  }

  public CommunicationException() {
    super(ErrorCode.COMMUNICATION_ERROR);
  }
}
