package com.hello.money.v1.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/v1/send")
public class TestController {

  @GetMapping("/test")
  public String test(@RequestHeader("x-account-id") final String encodedId, @RequestHeader("x-account-name") final String encodedName) {
    final String accountId = URLDecoder.decode(encodedId, StandardCharsets.UTF_8);
    final String accountName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
    log.info("x-account-id -> {}, x-account-name -> {}", accountId, accountName);
    return "Welcome to the Money Service.";
  }
}
