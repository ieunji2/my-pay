package com.hello.account;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.RestDocumentationContextProvider;

@TestConfiguration
public class RestDocsConfig {

  @Bean
  public RestDocumentationContextProvider restDocumentationContextProvider() {
    return new ManualRestDocumentation();
  }
}
