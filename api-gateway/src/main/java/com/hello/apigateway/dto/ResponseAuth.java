package com.hello.apigateway.dto;

import org.springframework.util.Assert;

public record ResponseAuth(Long id, String name, boolean isValid) {
  public ResponseAuth {
    Assert.notNull(id, "ID는 필수입니다.");
    Assert.hasText(name, "이름은 필수입니다.");
  }
}
