package com.hello.apigateway.dto;

import org.springframework.util.Assert;

public record AuthResponse(Long id, String name, boolean isValid) {
  public AuthResponse {
    Assert.notNull(id, "ID는 필수입니다.");
    Assert.hasText(name, "이름은 필수입니다.");
  }
}
