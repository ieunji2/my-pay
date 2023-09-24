package com.hello.account;

import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;

public class AccountSteps {

  public static RegisterAccountRequest 계정등록요청_생성() {
    final String name = "이름";
    final String email = "이메일";
    return new RegisterAccountRequest(name, email);
  }

  public static ExtractableResponse<Response> 계정등록요청(final RegisterAccountRequest request, final RequestSpecification spec) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      SnippetsConstants.REQUEST_FIELDS_SNIPPET,
                                      SnippetsConstants.RESPONSE_FIELDS_SNIPPET))
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .body(request)
                      .when()
                      .post("/v1/accounts")
                      .then()
                      .log().all().extract();
  }

  public static ExtractableResponse<Response> 계정조회요청(final Long accountId, final RequestSpecification spec) {

    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      SnippetsConstants.PATH_PARAMETERS_SNIPPET,
                                      SnippetsConstants.RESPONSE_FIELDS_SNIPPET))
                      .when()
                      .get("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }

  public static ModifyAccountRequest 계정수정요청_생성() {
    return new ModifyAccountRequest("이름 수정", "이메일 수정", false);
  }

  static ExtractableResponse<Response> 계정수정요청(final Long accountId, final RequestSpecification spec) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      SnippetsConstants.PATH_PARAMETERS_SNIPPET,
                                      SnippetsConstants.REQUEST_FIELDS_SNIPPET
                                              .and(
                                                      fieldWithPath("isValid").description("계정 상태").optional()),
                                      SnippetsConstants.RESPONSE_FIELDS_SNIPPET))
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .body(계정수정요청_생성())
                      .when()
                      .put("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }

  static ExtractableResponse<Response> 계정삭제요청(final Long accountId, final RequestSpecification spec) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      SnippetsConstants.PATH_PARAMETERS_SNIPPET,
                                      SnippetsConstants.RESPONSE_FIELDS_SNIPPET))
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .delete("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }
}