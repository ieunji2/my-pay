package com.hello.account;

import com.hello.account.v1.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class AccountApiTest extends ApiTest {

  @Autowired
  AccountRepository accountRepository;

  @Override
  @BeforeEach
  void setUp() {
    super.setUp();
    계정을_등록한다();
  }

  void 계정을_등록한다() {
    final var response = AccountSteps.계정등록요청(AccountSteps.계정등록요청_생성());
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
  }

  private static Stream<Arguments> accountIdParam() {
    return Stream.of(
            arguments(1L)
    );
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("계정을 조회한다")
  void findAccount(final Long accountId) {
    final var response = AccountSteps.계정조회요청(accountId);
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("name")).isEqualTo("이름");
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("계정을 수정한다")
  void modifyAccount(final Long accountId) {
    final var response = AccountSteps.계정수정요청(accountId);
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId).get().getName()).isEqualTo("이름 수정");
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("계정을 삭제한다")
  void removeAccount(final Long accountId) {
    final var response = AccountSteps.계정삭제요청(accountId);
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId)).isEmpty();
  }
}
