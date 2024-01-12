package com.hello.money;

import com.hello.money.config.RestDocsConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.snippet.Snippet;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiTest {

  @LocalServerPort
  private int port;

  @Autowired
  private DatabaseCleanup databaseCleanup;

  @Autowired
  RestDocumentationContextProvider restDocumentation;

  protected RequestSpecification spec;

  @BeforeEach
  void setUp() {
    if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
      RestAssured.port = port;
      databaseCleanup.afterPropertiesSet();
    }
    databaseCleanup.execute();

    this.spec = new RequestSpecBuilder()
            .addFilter(
                    documentationConfiguration(this.restDocumentation)
                            .operationPreprocessors()
                            .withRequestDefaults(prettyPrint())
                            .withResponseDefaults(prettyPrint()))
            .build();
  }

  RequestSpecification getFilter(Snippet... snippets) {
    return RestAssured.given(spec).log().all()
                      .filter(
                              document(
                                      SnippetsConstants.IDENTIFIER,
                                      snippets));
  }

  String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
