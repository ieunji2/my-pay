package com.hello.apigateway.common.exception;

public record ErrorResponse(
        int statusCode,
        String errorCode,
        String errorMessage) {

  private ErrorResponse(final ErrorCode errorCode) {
    this(
            errorCode.getStatusCode(),
            errorCode.getErrorCode(),
            errorCode.getErrorMessage());
  }

  public static ErrorResponse of(final ErrorCode errorCode) {
    return new ErrorResponse(errorCode);
  }
}
