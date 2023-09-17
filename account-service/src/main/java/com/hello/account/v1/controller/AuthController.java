package com.hello.account.v1.controller;

import com.hello.account.v1.dto.ResponseAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final Environment env;

  @GetMapping
  public String status() {
    return String.format("Money Service is working on PORT %s", env.getProperty("local.server.port"));
  }

  @GetMapping("/check")
  public ResponseAuth checkAccessToken(@RequestHeader("Authorization") final String authorizationHeader) {

    log.info("authorizationHeader -> {}", authorizationHeader);

    final String accessToken = authorizationHeader.replace("Bearer ", "");

    if ("123".equals(accessToken)) {
      return new ResponseAuth(1L, "이름", true);
    } else {
      return new ResponseAuth(1L, "이름", false);
    }
  }
}
