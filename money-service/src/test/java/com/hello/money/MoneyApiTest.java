package com.hello.money;

import com.hello.money.v1.dto.AddMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MoneyApiTest extends ApiTest {

  private static Stream<Arguments> accountIdParam() {
    return Stream.of(
            arguments(1L)
    );
  }

  private static Stream<Arguments> addMoneyRequestParam() {
    return Stream.of(
            arguments(1L, new AddMoneyRequest(BigInteger.valueOf(3000), "적요"))
    );
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(1L, new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요"))
    );
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 생성한다")
  void createWallet(final Long accountId) {
    //given, when
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

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 조회한다")
  void getWallet(final Long accountId) {
    //given
    createWallet(accountId);

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

  @ParameterizedTest
  @MethodSource("addMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 지갑의 잔액을 충전한다")
  void addMoney(final Long accountId, final AddMoneyRequest request) {
    //given
    createWallet(accountId);

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
    assertThat(response.jsonPath().getString("balance")).isEqualTo("3000");
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 금액을 송금한다")
  void sendMoney(final Long accountId, final SendMoneyRequest request) {
    //given
    addMoney(accountId, new AddMoneyRequest(BigInteger.valueOf(3000), "적요"));
    createWallet(2L);

    //when
    final var response = RestAssured.given().log().all()
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .header("x-account-id", encode(String.valueOf(accountId)))
                                    .body(request)
                                    .when()
                                    .post("/v1/moneys/send")
                                    .then().log().all()
                                    .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("balance")).isEqualTo("1000");
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}