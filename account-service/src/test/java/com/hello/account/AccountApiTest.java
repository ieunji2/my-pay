package com.hello.account;

import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import com.hello.account.v1.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
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
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class AccountApiTest extends ApiTest {

  @Autowired
  AccountRepository accountRepository;

  private static Stream<Arguments> accountIdParam() {
    return Stream.of(
            arguments(1L));
  }

  @Test
  @DisplayName("계정을 등록한다")
  void registerAccount() {
    //given
    final Snippet[] snippets = {
            SnippetsConstants.REQUEST_FIELDS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    final var request = new RegisterAccountRequest("이름", "이메일");

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/v1/accounts")
            .then()
            .log().all().extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("아이디로 계정을 조회한다")
  void findAccount(final Long accountId) {
    //given
    registerAccount();

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    //when
    final var response = getFilter(snippets)
            .when()
            .get("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.jsonPath().getString("name")).isEqualTo("이름");
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("계정을 수정한다")
  void modifyAccount(final Long accountId) {
    //given
    registerAccount();

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.REQUEST_FIELDS_SNIPPET.and(
                    fieldWithPath("isValid").description("계정 상태").optional()),
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

    final var request = new ModifyAccountRequest("이름 수정", "이메일 수정", false);

    //when
    final var response = getFilter(snippets)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .put("/v1/accounts/{accountId}", accountId)
            .then().log().all()
            .extract();

    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    assertThat(accountRepository.findById(accountId).get().getName()).isEqualTo("이름 수정");
  }

  @ParameterizedTest
  @MethodSource("accountIdParam")
  @DisplayName("계정을 삭제한다")
  void removeAccount(final Long accountId) {
    //given
    registerAccount();

    final Snippet[] snippets = {
            SnippetsConstants.PATH_PARAMETERS_SNIPPET,
            SnippetsConstants.RESPONSE_FIELDS_SNIPPET};

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

  private RequestSpecification getFilter(Snippet... snippets) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      snippets));
  }
}
