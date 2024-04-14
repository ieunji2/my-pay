package com.hello.money;

import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParameterFactory {

  public static Stream<Arguments> accountParam() {
    return Stream.of(
            arguments(
                    new Account(1L, "이름")));
  }

  private static Stream<Arguments> chargeMoneyRequestParam() {

    return Stream.of(
            arguments(
                    new Account(1L, "이름"),
                    new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(
                    new Account(1L, "이름"),
                    new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestInvalidInputValueParam() {
    return Stream.of(
            arguments(
                    new Account(1L, "이름"),
                    new SendMoneyRequest(null, BigInteger.ZERO, "적요")));
  }
}