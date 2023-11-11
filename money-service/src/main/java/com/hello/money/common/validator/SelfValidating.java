package com.hello.money.common.validator;

import jakarta.validation.*;

import java.util.Set;

public class SelfValidating {

  private static final Validator validator;

  static {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  public static <T> void validateSelf(final T object) {
    final Set<ConstraintViolation<T>> violations = validator.validate(object);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
