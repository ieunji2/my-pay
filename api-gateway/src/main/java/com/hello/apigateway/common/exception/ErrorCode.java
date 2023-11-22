package com.hello.apigateway.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(500, "G001", "Server Error"),
  RESPONSE_STATUS_ERROR(500, "G002", "Server Error"),
  COMMUNICATION_ERROR(500, "G003", "HTTP Communication Error"),
  UNAUTHORIZED(401, "G004", "Unauthorized"),
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
