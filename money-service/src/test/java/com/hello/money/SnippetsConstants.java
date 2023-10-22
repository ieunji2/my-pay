package com.hello.money;

import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

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

  public static final ResponseFieldsSnippet RESPONSE_FIELDS_SNIPPET = responseFields(
          fieldWithPath("id").description("지갑 아이디"),
          fieldWithPath("accountId").description("계정 아이디"),
          fieldWithPath("balance").description("아이디"));
}