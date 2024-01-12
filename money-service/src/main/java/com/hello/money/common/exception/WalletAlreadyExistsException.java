package com.hello.money.common.exception;

public class WalletAlreadyExistsException extends BusinessException {

  public WalletAlreadyExistsException(final String message) {
    super(message, ErrorCode.WALLET_ALREADY_EXISTS);
  }

  public WalletAlreadyExistsException() {
    super(ErrorCode.WALLET_ALREADY_EXISTS);
  }
}
