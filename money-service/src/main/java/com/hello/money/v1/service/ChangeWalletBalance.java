package com.hello.money.v1.service;

import com.hello.money.domain.Wallet;

import java.math.BigInteger;

@FunctionalInterface
public interface ChangeWalletBalance {
  Wallet apply(Wallet wallet, BigInteger amount);
}
