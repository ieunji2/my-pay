package com.hello.apigateway.common.exception;

public record ErrorResponse(
        int status,
        String code,
        String message) {

  private ErrorResponse(final ErrorCode errorCode) {
    this(
            errorCode.getStatus(),
            errorCode.getCode(),
            errorCode.getMessage());
  }

  public static ErrorResponse of(final ErrorCode errorCode) {
    return new ErrorResponse(errorCode);
  }
}
