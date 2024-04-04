package com.hello.account.v1.controller;

import com.hello.account.v1.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auths")
public class AuthController {

  @GetMapping("/check")
  public AuthResponse checkAccessToken(@RequestHeader("Authorization") final String authorizationHeader) {

    log.info("authorizationHeader -> {}", authorizationHeader);

    final String accessToken = authorizationHeader.replace("Bearer ", "");

    if ("123".equals(accessToken)) {
      return new AuthResponse(1L, "이름", true);
    } else if ("456".equals(accessToken)) {
      return new AuthResponse(2L, "이름2", true);
    } else if ("789".equals(accessToken)) {
      return new AuthResponse(3L, "이름3", true);
    } else {
      return new AuthResponse(4L, "이름3", false);
    }
  }
}
