package com.hello.money.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(500, "M001", "Server Error"),
  NOT_FOUND(404, "M002", "Not Found"),
  INVALID_INPUT_VALUE(400, "M003", "Invalid Input Value"),
  WALLET_ALREADY_EXISTS(400, "M004", "Wallet Already Exists"),
  WALLET_NOT_FOUND(400, "M005", "Wallet Not Found"),
  INSUFFICIENT_BALANCE(400, "M006", "Insufficient Balance"),
  INVALID_ACCOUNT(400, "M007", "Invalid Account"),
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
