package com.hello.apigateway.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(500, "G001", "Server Error"),
  RESPONSE_STATUS_ERROR(500, "G002", "Server Error"),
  COMMUNICATION_ERROR(500, "G003", "HTTP Communication Error"),
  UNAUTHORIZED(401, "G004", "Unauthorized"),
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
