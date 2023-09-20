package com.hello.account.v1.dto;

import org.springframework.util.Assert;

public record RegisterAccountRequest(String name, String email) {
  public RegisterAccountRequest {
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
  }
}