package com.hello.apigateway.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ErrorResponse(
        int status,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ValidationError> errors) {

  private ErrorResponse(final ErrorCode errorCode) {
    this(
            errorCode.getStatus(),
            errorCode.getCode(),
            errorCode.getMessage(),
            new ArrayList<>());
  }

  private ErrorResponse(final ErrorCode errorCode, final List<ValidationError> errors) {
    this(
            errorCode.getStatus(),
            errorCode.getCode(),
            errorCode.getMessage(),
            errors);
  }

  public static ErrorResponse of(final ErrorCode errorCode) {
    return new ErrorResponse(errorCode);
  }

  public static ErrorResponse of(final ErrorCode errorCode, final Set<ConstraintViolation<?>> constraintViolations) {
    return new ErrorResponse(errorCode, ValidationError.of(constraintViolations));
  }

  private record ValidationError(
          String field,
          String value,
          String message) {

    private static List<ValidationError> of(final Set<ConstraintViolation<?>> constraintViolations) {
      return constraintViolations
              .stream()
              .map(constraintViolation -> new ValidationError(
                      String.valueOf(constraintViolation.getPropertyPath()),
                      String.valueOf(constraintViolation.getInvalidValue()),
                      constraintViolation.getMessage()
              ))
              .collect(Collectors.toList());
    }
  }
}
