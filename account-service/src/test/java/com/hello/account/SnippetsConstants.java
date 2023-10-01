package com.hello.account;

import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestDocumentation;

public class SnippetsConstants {

  public static final String IDENTIFIER = "{class-name}/{method-name}";

  public static final PathParametersSnippet PATH_PARAMETERS_SNIPPET = RequestDocumentation.pathParameters(
          RequestDocumentation.parameterWithName("accountId").description("아이디"));

  public static final RequestFieldsSnippet REQUEST_FIELDS_SNIPPET = PayloadDocumentation.requestFields(
          PayloadDocumentation.fieldWithPath("name").description("이름"),
          PayloadDocumentation.fieldWithPath("email").description("이메일"));

  public static final ResponseFieldsSnippet RESPONSE_FIELDS_SNIPPET = PayloadDocumentation.responseFields(
          PayloadDocumentation.fieldWithPath("id").description("아이디"),
          PayloadDocumentation.fieldWithPath("name").description("이름"),
          PayloadDocumentation.fieldWithPath("email").description("이메일"),
          PayloadDocumentation.fieldWithPath("isValid").description("계정 상태"));
}