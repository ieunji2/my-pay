package com.hello.money;

import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.util.ArrayList;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

public class SnippetsConstants {

  public static final String IDENTIFIER = "{class-name}/{method-name}";

  public static final RequestHeadersSnippet REQUEST_HEADERS_SNIPPET = requestHeaders(
          headerWithName("x-account-id").description("아이디"),
          headerWithName("x-account-name").description("이름"));

  public static final RequestFieldsSnippet REQUEST_FIELDS_SNIPPET = requestFields(
          fieldWithPath("amount").description("금액"),
          fieldWithPath("summary").description("적요"));

  public static final ResponseFieldsSnippet SUCCESS_RESPONSE_FIELDS_SNIPPET = responseFields(
          fieldWithPath("id").description("지갑 아이디"),
          fieldWithPath("accountId").description("계정 아이디"),
          fieldWithPath("balance").description("잔액"));

  public static final ResponseFieldsSnippet ERROR_RESPONSE_FIELDS_SNIPPET = responseFields(
          fieldWithPath("statusCode").description("상태 코드"),
          fieldWithPath("errorCode").description("오류 코드"),
          fieldWithPath("errorMessage").description("오류 메세지"),
          fieldWithPath("errors").description("입력값 유효성 검증 오류 목록")
                                 .type(ArrayList.class)
                                 .optional())
          .andWithPrefix(
                  "errors.[].",
                  fieldWithPath("field").description("필드명")
                                        .type(String.class)
                                        .optional(),
                  fieldWithPath("value").description("입력값")
                                        .type(String.class)
                                        .optional(),
                  fieldWithPath("message").description("오류 메세지")
                                          .type(String.class)
                                          .optional());
}