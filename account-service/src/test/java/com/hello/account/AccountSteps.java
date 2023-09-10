package com.hello.account;

import com.hello.account.v1.dto.ModifyAccountRequest;
import com.hello.account.v1.dto.RegisterAccountRequest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.MediaType;

public class AccountSteps {

  public static RegisterAccountRequest 계정등록요청_생성() {
    final String name = "이름";
    final String email = "이메일";
    return new RegisterAccountRequest(name, email);
  }

  public static ExtractableResponse<Response> 계정등록요청(final RegisterAccountRequest request) {
    return RestAssured.given().log().all()
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .body(request)
                      .when()
                      .post("/v1/accounts")
                      .then()
                      .log().all().extract();
  }

  public static ExtractableResponse<Response> 계정조회요청(final Long accountId) {
    return RestAssured.given().log().all()
                      .when()
                      .get("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }

  public static ModifyAccountRequest 계정수정요청_생성() {
    return new ModifyAccountRequest("이름 수정", "이메일 수정", true);
  }

  static ExtractableResponse<Response> 계정수정요청(final Long accountId) {
    return RestAssured.given().log().all()
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .body(계정수정요청_생성())
                      .when()
                      .put("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }

  static ExtractableResponse<Response> 계정삭제요청(final Long accountId) {
    return RestAssured.given().log().all()
                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .delete("/v1/accounts/{accountId}", accountId)
                      .then().log().all()
                      .extract();
  }
}