package com.hello.account;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;

import java.util.ArrayList;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

public class SnippetsConstants {

  public static final String IDENTIFIER = "{class-name}/{method-name}";

  public static final PathParametersSnippet PATH_PARAMETERS_SNIPPET = pathParameters(
          parameterWithName("accountId").description("아이디"));

  public static final RequestFieldsSnippet REQUEST_FIELDS_SNIPPET = requestFields(
          fieldWithPath("name").description("이름"),
          fieldWithPath("email").description("이메일"));

  public static final ResponseFieldsSnippet SUCCESS_RESPONSE_FIELDS_SNIPPET = responseFields(
          fieldWithPath("id").description("아이디"),
          fieldWithPath("name").description("이름"),
          fieldWithPath("email").description("이메일"),
          fieldWithPath("isValid").description("계정 상태"));

  public static final ResponseFieldsSnippet ERROR_RESPONSE_FIELDS_SNIPPET = responseFields(
          fieldWithPath("status").description("상태 코드"),
          fieldWithPath("code").description("오류 코드"),
          fieldWithPath("message").description("오류 메세지"),
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