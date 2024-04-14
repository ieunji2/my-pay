package com.hello.account;

import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ParameterFactory {

  private static Stream<Arguments> registerAccountRequestParam() {
    return Stream.of(
            arguments(
                    1L,
                    new RegisterAccountRequest("이름", "mypay@test.com")));
  }

  private static Stream<Arguments> modifyAccountRequestParam() {
    return Stream.of(
            arguments(
                    1L,
                    new RegisterAccountRequest("이름", "mypay@test.com"),
                    new ModifyAccountRequest("이름 수정", "yourpay@test.com", false)));
  }

  private static Stream<Arguments> registerAccountRequestInvalidInputValueParam() {
    return Stream.of(
            arguments(
                    new RegisterAccountRequest(" ", "이메일?")));
  }
}
