package com.hello.money;

import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.repository.WalletRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyApiTest extends ApiTest {

  @Autowired
  private WalletRepository walletRepository;

  @BeforeEach
  void setUp() {
    super.setUp();
    walletRepository.deleteAll();
  }

  @Test
  @DisplayName("인증된 계정의 아이디로 지갑을 생성한다")
  void createWallet() {
    //given
    final Long accountId = 1L;

    //when
    final var response = RestAssured.given().log().all()
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .header("x-account-id", encode(String.valueOf(accountId)))
                                    .when()
                                    .post("/v1/moneys")
                                    .then().log().all()
                                    .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(accountId));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("0");
  }

  @Test
  @DisplayName("인증된 계정의 아이디로 지갑을 조회한다")
  void getWallet() {
    //given
    createWallet();
    final Long accountId = 1L;

    //when
    final var response = RestAssured.given().log().all()
                                    .header("x-account-id", encode(String.valueOf(accountId)))
                                    .when()
                                    .get("/v1/moneys")
                                    .then().log().all()
                                    .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(accountId));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("0");
  }

  @Test
  @DisplayName("인증된 계정의 아이디로 지갑의 잔액을 충전한다")
  void addMoney() {
    //given
    createWallet();
    final Long accountId = 1L;
    final var request = new AddMoneyRequest(BigInteger.valueOf(1000));

    //when
    final var response = RestAssured.given().log().all()
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .header("x-account-id", encode(String.valueOf(accountId)))
                                    .body(request)
                                    .when()
                                    .post("/v1/moneys/charge")
                                    .then().log().all()
                                    .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(accountId));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("1000");
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}