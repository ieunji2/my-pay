package com.hello.account.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(500, "A001", "Server Error"),
  NOT_FOUND(404, "A002", "Not Found"),
  INVALID_INPUT_VALUE(400, "A003", "Invalid Input Value"),
  ACCOUNT_NOT_FOUND(400, "A004", "Account Not Found"),
  ;

  private final int statusCode;
  private final String errorCode;
  private final String errorMessage;

  ErrorCode(final int statusCode, final String errorCode, final String errorMessage) {
    this.statusCode = statusCode;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
