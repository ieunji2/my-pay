package com.hello.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

  @Test
  @DisplayName("계정을 수정한다")
  void updateAccount() {
    final Account account = new Account("이름", "이메일");

    account.updateAccount("이름 수정", "이메일 수정", false);

    assertThat(account.getName()).isEqualTo("이름 수정");
    assertThat(account.getEmail()).isEqualTo("이메일 수정");
    assertThat(account.isValid()).isEqualTo(false);
  }
}