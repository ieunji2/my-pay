package com.hello.money.common.exception;

public class WalletNotFoundException extends BusinessException {

  public WalletNotFoundException(final String message) {
    super(message, ErrorCode.WALLET_NOT_FOUND);
  }

  public WalletNotFoundException() {
    super(ErrorCode.WALLET_NOT_FOUND);
  }
}
