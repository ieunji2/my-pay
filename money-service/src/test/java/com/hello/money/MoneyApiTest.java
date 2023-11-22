package com.hello.money;

import com.hello.money.common.exception.ErrorCode;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Snippet;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

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

  private static Stream<Arguments> sendMoneyRequestInvalidInputValueParam() {
    return Stream.of(
            arguments(new Account(1L, "이름"), new SendMoneyRequest(null, BigInteger.ZERO, "적요")));
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 생성한다")
  void createWallet(final Account account) {
    //given, when
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .post("/v1/money")
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
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .get("/v1/money")
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
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .body(request)
            .when()
            .post("/v1/money/charge")
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
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .body(request)
            .when()
            .post("/v1/money/send")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("balance")).isEqualTo("1000");
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("지갑이 이미 존재하는 경우 지갑 생성 시 오류 응답을 반환한다")
  void checkWalletAlreadyExistsExceptionErrorResponse(final Account account) {
    //given, when
    createWallet(account);

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .post("/v1/money")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getStatus());
    assertThat(response.jsonPath().getString("code")).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getCode());
    assertThat(response.jsonPath().getString("message")).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getMessage());
  }

  @ParameterizedTest
  @MethodSource("accountParam")
  @DisplayName("지갑 조회 시 없으면 오류 응답을 반환한다")
  void checkWalletNotFoundExceptionErrorResponse(final Account account) {
    //given
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .when()
            .get("/v1/money")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getStatus());
    assertThat(response.jsonPath().getString("code")).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getCode());
    assertThat(response.jsonPath().getString("message")).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getMessage());
  }

  @ParameterizedTest
  @MethodSource("sendMoneyRequestInvalidInputValueParam")
  @DisplayName("유효하지 않은 값으로 금액 송금 시 오류 응답을 반환한다")
  void checkConstraintViolationExceptionErrorResponse(final Account account, final SendMoneyRequest request) {
    //given
    chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));
    createWallet(new Account(2L, "이름2"));

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET.and(
                    fieldWithPath("receiverWalletId").description("수취인 지갑 아이디")),
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", encode(String.valueOf(account.id())))
            .header("x-account-name", encode(account.name()))
            .body(request)
            .when()
            .post("/v1/money/send")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getStatus());
    assertThat(response.jsonPath().getString("code")).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getCode());
    assertThat(response.jsonPath().getString("message")).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    assertThat(response.jsonPath().getList("errors")).hasSize(2);
  }

  @Test
  @DisplayName("요청 핸들러를 찾을 수 없으면 오류 응답을 반환한다")
  void checkNoHandlerFoundException() {
    //given
    final String noMappingPath = "/v1/test";

    final Snippet[] snippets = {
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .when()
            .get(noMappingPath)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(ErrorCode.NOT_FOUND.getStatus());
    assertThat(response.jsonPath().getString("code")).isEqualTo(ErrorCode.NOT_FOUND.getCode());
    assertThat(response.jsonPath().getString("message")).isEqualTo(ErrorCode.NOT_FOUND.getMessage());
  }
}