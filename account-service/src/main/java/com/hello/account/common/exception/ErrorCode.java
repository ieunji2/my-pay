package com.hello.account.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(500, "A001", "Server Error"),
  NOT_FOUND(404, "A002", "Not Found"),
  INVALID_INPUT_VALUE(400, "A003", "Invalid Input Value"),
  ACCOUNT_NOT_FOUND(400, "A004", "Account Not Found"),
  ;

  private final int status;
  private final String code;
  private final String message;

  ErrorCode(final int status, final String code, final String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
