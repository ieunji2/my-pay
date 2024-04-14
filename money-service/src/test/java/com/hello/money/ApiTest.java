package com.hello.money;

import com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.restdocs.snippet.Snippet;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class) //(1)RestDocumentationExtension 적용
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTest {

  @LocalServerPort
  private int port;

  @Autowired
  private DatabaseCleanup databaseCleanup;

  private RequestSpecification spec; //(2)Rest Assured로 HTTP 요청에 사용할 객체

  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {

    databaseCleanup.afterPropertiesSet();
    databaseCleanup.execute();

    if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
      RestAssured.port = port;
    }

    spec = new RequestSpecBuilder()
            .addFilter(documentationConfiguration(restDocumentation) //(3)문서 구성을 위한 필터 추가
                               .operationPreprocessors() //(4)전처리기 설정
                               .withRequestDefaults(prettyPrint()) //(5)내용이 예쁘게 출력되도록 요청 전처리기 추가
                               .withResponseDefaults(prettyPrint())) //(6)내용이 예쁘게 출력되도록 응답 전처리기 추가
            .build();
  }

  RequestSpecification getWrapperFilter(final Snippet[] snippets) {
    return RestAssured
            .given(spec).log().all()
            .filter(getRestDocumentationWrapper(snippets));
  }

  RequestSpecification getFilter(final Snippet[] snippets) {
    return RestAssured
            .given(spec).log().all()
            .filter(getRestDocumentation(snippets));
  }

  static RestDocumentationFilter getRestDocumentationWrapper(final Snippet[] snippets) {
    return RestAssuredRestDocumentationWrapper.document(SnippetsConstants.IDENTIFIER, snippets);
  }

  static RestDocumentationFilter getRestDocumentation(final Snippet[] snippets) {
    return RestAssuredRestDocumentation.document(SnippetsConstants.IDENTIFIER, snippets);
  }

  static String encode(final String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
