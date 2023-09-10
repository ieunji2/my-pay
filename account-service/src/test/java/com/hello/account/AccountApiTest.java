package com.hello.account;

import com.hello.account.v1.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountApiTest extends ApiTest {

  @Autowired
  AccountRepository accountRepository;

  @Test
  void 계정등록() {
    final var request = AccountSteps.계정등록요청_생성();

    final var response = AccountSteps.계정등록요청(request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void 계정조회() {
    AccountSteps.계정등록요청(AccountSteps.계정등록요청_생성());
    Long accountId = 1L;

    final var response = AccountSteps.계정조회요청(accountId);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("name")).isEqualTo("이름");
  }

  @Test
  void 계정수정() {
    AccountSteps.계정등록요청(AccountSteps.계정등록요청_생성());
    final long accountId = 1L;
    final var response = AccountSteps.계정수정요청(accountId);
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(1L).get().getName()).isEqualTo("이름 수정");
  }

  @Test
  void 계정삭제() {
    AccountSteps.계정등록요청(AccountSteps.계정등록요청_생성());
    final long accountId = 1L;
    final var response = AccountSteps.계정삭제요청(accountId);
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(1L)).isEmpty();
  }
}
