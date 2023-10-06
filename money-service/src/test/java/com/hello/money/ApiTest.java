package com.hello.money;

import com.hello.money.v1.repository.WalletRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTest {

  @LocalServerPort
  private int port;

  @Autowired
  WalletRepository walletRepository;

  @BeforeEach
  void setUp() {
    if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
      RestAssured.port = port;
    }

    walletRepository.deleteAll();
  }
}