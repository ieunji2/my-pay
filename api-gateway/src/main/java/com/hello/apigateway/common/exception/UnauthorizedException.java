package com.hello.apigateway.common.exception;

public class UnauthorizedException extends BusinessException {

  public UnauthorizedException(final String message) {
    super(message, ErrorCode.UNAUTHORIZED);
  }

  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED);
  }
}
