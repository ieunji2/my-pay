package com.hello.account;

import com.hello.account.domain.Account;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AccountTest {

  @Test
  void updateAccount() {
    final Account account = new Account("이름", "이메일");

    account.updateAccount("이름 수정", "이메일 수정", false);

    assertThat(account.getName()).isEqualTo("이름 수정");
    assertThat(account.getEmail()).isEqualTo("이메일 수정");
    assertThat(account.isValid()).isEqualTo(false);
  }
}