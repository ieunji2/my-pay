package com.hello.account.v1.dto;

import org.springframework.util.Assert;

public record ModifyAccountRequest(String name, String email, boolean isValid) {
  public ModifyAccountRequest {
    Assert.hasText(name, "이름은 필수입니다");
    Assert.hasText(email, "이메일은 필수입니다");
  }
}
