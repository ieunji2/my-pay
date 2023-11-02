package com.hello.money;

import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Snippet;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

class MoneyApiTest extends ApiTest {

  private static Stream<Arguments> accountParam() {
    return Stream.of(
            arguments(new Account(1L, "이름")));
  }

  private static Stream<Arguments> chargeMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요")));
  }

  private static Stream<Arguments> sendMoneyRequestParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(2L, BigInteger.valueOf(2000), "적요")));
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 생성한다")
  void createWallet(final Account account) {
    //given, when
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .post("/v1/moneys")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(account.id()));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("0");
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 조회한다")
  void getWallet(final Account account) {
    //given
    createWallet(account);

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .get("/v1/moneys")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(account.id()));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("0");
  }

  @ParameterizedTest
  @MethodSource("chargeMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 지갑의 잔액을 충전한다")
  void chargeMoney(final Account account, final ChargeMoneyRequest request) {
    //given
    createWallet(account);

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .body(request)
            .when()
            .post("/v1/moneys/charge")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("accountId")).isEqualTo(String.valueOf(account.id()));
    assertThat(response.jsonPath().getString("balance")).isEqualTo("3000");
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 금액을 송금한다")
  void sendMoney(final Account account, final SendMoneyRequest request) {
    //given
    chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));
    createWallet(new Account(2L, "이름2"));

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET.and(
                    fieldWithPath("receiverWalletId").description("수취인 지갑 아이디")),
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
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

  private RequestSpecification getFilter(Snippet... snippets) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      snippets));
  }
}