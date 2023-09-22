package com.hello.account.health;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/health")
public class HealthCheckController {

  private final Environment env;

  @GetMapping("/ping")
  public String ping() {
    return String.format("Account Service is working on PORT %s", env.getProperty("local.server.port"));
  }
}
