package com.hello.account;

import com.hello.account.common.exception.ErrorCode;
import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import com.hello.account.v1.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Snippet;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AccountApiTest extends ApiTest {

  @Autowired
  private AccountRepository accountRepository;

  private static Stream<Arguments> registerAccountRequestParam() {
    return Stream.of(
            arguments(
                    1L,
                    new RegisterAccountRequest("이름", "mypay@test.com")));
  }

  private static Stream<Arguments> modifyAccountRequestParam() {
    return Stream.of(
            arguments(
                    1L,
                    new RegisterAccountRequest("이름", "mypay@test.com"),
                    new ModifyAccountRequest("이름 수정", "yourpay@test.com", false)));
  }

  private static Stream<Arguments> registerAccountRequestInvalidInputValueParam() {
    return Stream.of(
            arguments(
                    new RegisterAccountRequest(" ", "이메일?")));
  }

  @ParameterizedTest
  @MethodSource("registerAccountRequestParam")
  @DisplayName("계정을 등록한다")
  void registerAccount(final Long accountId, final RegisterAccountRequest request) {
    //given
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_FIELDS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/v1/accounts")
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId).get().getName()).isEqualTo(request.name());
  }

  @ParameterizedTest
  @MethodSource("registerAccountRequestParam")
  @DisplayName("아이디로 계정을 조회한다")
  void findAccount(final Long accountId, final RegisterAccountRequest request) {
    //given
    registerAccount(accountId, request);

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .when()
            .get("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("name")).isEqualTo(request.name());
  }

  @ParameterizedTest
  @MethodSource("modifyAccountRequestParam")
  @DisplayName("계정을 수정한다")
  void modifyAccount(
          final Long accountId,
          final RegisterAccountRequest registerRequest,
          final ModifyAccountRequest modifyRequest) {
    //given
    registerAccount(accountId, registerRequest);

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET.and(
                    fieldWithPath("isValid").description("계정 상태").optional()),
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(modifyRequest)
            .when()
            .put("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId).get().getName()).isEqualTo(modifyRequest.name());
  }

  @ParameterizedTest
  @MethodSource("registerAccountRequestParam")
  @DisplayName("계정을 삭제한다")
  void removeAccount(final Long accountId, final RegisterAccountRequest request) {
    //given
    registerAccount(accountId, request);

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.SUCCESS_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId)).isEmpty();
  }

  @Test
  @DisplayName("계정 조회 시 없으면 오류 응답을 반환한다")
  void checkAccountNotFoundException() {
    //given
    final Long accountId = 1L;

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .when()
            .get("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getStatus());
    assertThat(response.jsonPath().getString("code")).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getCode());
    assertThat(response.jsonPath().getString("message")).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND.getMessage());
  }

  @ParameterizedTest
  @MethodSource("registerAccountRequestInvalidInputValueParam")
  @DisplayName("유효하지 않은 값으로 계정 등록 시 오류 응답을 반환한다")
  void checkMethodArgumentNotValidException(final RegisterAccountRequest request) {
    //given
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_FIELDS_SNIPPET,
            SnippetsConstants.ERROR_RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/v1/accounts")
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
