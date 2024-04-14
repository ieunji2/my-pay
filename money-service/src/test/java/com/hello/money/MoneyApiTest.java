package com.hello.money;

import com.hello.money.common.exception.ErrorCode;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.AccountResponse;
import com.hello.money.v1.dto.ChargeMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.service.ExchangeApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Snippet;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class MoneyApiTest extends ApiTest {

  @MockBean
  private ExchangeApi exchangeApi;

  @ParameterizedTest
  @MethodSource("com.hello.money.ParameterFactory#accountParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 생성한다")
  void createWallet(final Account account) {
    //given, when
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    final var response = getWrapperFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("x-account-id", (String.valueOf(account.id())))
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
  @MethodSource("com.hello.money.ParameterFactory#accountParam")
  @DisplayName("인증된 계정의 아이디로 지갑을 조회한다")
  void getWallet(final Account account) {
    //given
    createWallet(account);

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getWrapperFilter(snippets)
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
  @MethodSource("com.hello.money.ParameterFactory#chargeMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 지갑의 잔액을 충전한다")
  void chargeMoney(final Account account, final ChargeMoneyRequest request) {
    //given
    createWallet(account);

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getWrapperFilter(snippets)
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
  @MethodSource("com.hello.money.ParameterFactory#sendMoneyRequestParam")
  @DisplayName("인증된 계정의 아이디로 금액을 송금한다")
  void sendMoney(final Account account, final SendMoneyRequest request) {
    //given
    chargeMoney(account, new ChargeMoneyRequest(BigInteger.valueOf(3000), "적요"));
    createWallet(new Account(2L, "이름2"));

    when(exchangeApi.getAccount(2L))
            .thenReturn(new AccountResponse(2L, "이름2", "mypay@test.com", true));

    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_HEADERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET.and(
                    fieldWithPath("receiverWalletId").description("수취인 지갑 아이디")),
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getWrapperFilter(snippets)
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
  @MethodSource("com.hello.money.ParameterFactory#accountParam")
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
    assertThat(response.statusCode()).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getStatusCode());
    assertThat(response.jsonPath().getString("errorCode")).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getErrorCode());
    assertThat(response.jsonPath().getString("errorMessage")).isEqualTo(ErrorCode.WALLET_ALREADY_EXISTS.getErrorMessage());
  }

  @ParameterizedTest
  @MethodSource("com.hello.money.ParameterFactory#accountParam")
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
    assertThat(response.statusCode()).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getStatusCode());
    assertThat(response.jsonPath().getString("errorCode")).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getErrorCode());
    assertThat(response.jsonPath().getString("errorMessage")).isEqualTo(ErrorCode.WALLET_NOT_FOUND.getErrorMessage());
  }

  @ParameterizedTest
  @MethodSource("com.hello.money.ParameterFactory#sendMoneyRequestInvalidInputValueParam")
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
    assertThat(response.statusCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getStatusCode());
    assertThat(response.jsonPath().getString("errorCode")).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getErrorCode());
    assertThat(response.jsonPath().getString("errorMessage")).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getErrorMessage());
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
    assertThat(response.statusCode()).isEqualTo(ErrorCode.NOT_FOUND.getStatusCode());
    assertThat(response.jsonPath().getString("errorCode")).isEqualTo(ErrorCode.NOT_FOUND.getErrorCode());
    assertThat(response.jsonPath().getString("errorMessage")).isEqualTo(ErrorCode.NOT_FOUND.getErrorMessage());
  }
}
