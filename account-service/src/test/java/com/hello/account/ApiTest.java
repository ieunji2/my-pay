package com.hello.account;

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

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTest {

  @LocalServerPort
  private int port;

  @Autowired
  private DatabaseCleanup databaseCleanup;

  private RequestSpecification spec;

  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {

    databaseCleanup.afterPropertiesSet();
    databaseCleanup.execute();

    if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
      RestAssured.port = port;
    }

    spec = new RequestSpecBuilder()
            .addFilter(documentationConfiguration(restDocumentation)
                               .operationPreprocessors()
                               .withRequestDefaults(prettyPrint())
                               .withResponseDefaults(prettyPrint()))
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
}
