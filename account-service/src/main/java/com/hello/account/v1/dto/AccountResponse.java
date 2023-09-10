package com.hello.account.v1.dto;

import org.springframework.util.Assert;

public record AccountResponse(Long id, String name, String email, boolean isValid) {
  public AccountResponse {
    Assert.notNull(id, "ID는 필수입니다.");
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
  }
}
