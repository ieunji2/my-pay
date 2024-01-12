package com.hello.account.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ErrorResponse(
        int statusCode,
        String errorCode,
        String errorMessage,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ValidationError> errors) {

  private ErrorResponse(final ErrorCode errorCode) {
    this(
            errorCode.getStatusCode(),
            errorCode.getErrorCode(),
            errorCode.getErrorMessage(),
            new ArrayList<>());
  }

  private ErrorResponse(final ErrorCode errorCode, final List<ValidationError> errors) {
    this(
            errorCode.getStatusCode(),
            errorCode.getErrorCode(),
            errorCode.getErrorMessage(),
            errors);
  }

  public static ErrorResponse of(final ErrorCode errorCode) {
    return new ErrorResponse(errorCode);
  }

  public static ErrorResponse of(final ErrorCode errorCode, final BindException e) {
    return new ErrorResponse(errorCode, ValidationError.of(e));
  }

  public static ErrorResponse of(final ErrorCode errorCode, final ConstraintViolationException e) {
    return new ErrorResponse(errorCode, ValidationError.of(e));
  }

  private record ValidationError(
          String field,
          String value,
          String message) {

    private static List<ValidationError> of(final BindException e) {
      return e.getBindingResult()
              .getFieldErrors()
              .stream()
              .map(fieldError -> new ValidationError(
                      fieldError.getField(),
                      (String) fieldError.getRejectedValue(),
                      fieldError.getDefaultMessage()
              ))
              .collect(Collectors.toList());
    }

    private static List<ValidationError> of(final ConstraintViolationException e) {
      return e.getConstraintViolations()
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
