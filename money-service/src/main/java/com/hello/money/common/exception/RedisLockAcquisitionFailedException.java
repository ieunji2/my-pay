package com.hello.money.common.exception;

public class RedisLockAcquisitionFailedException extends BusinessException {

  public RedisLockAcquisitionFailedException(final String message) {
    super(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }

  public RedisLockAcquisitionFailedException() {
    super(ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
