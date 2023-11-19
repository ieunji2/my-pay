package com.hello.money.config;

import com.hello.money.v1.service.ExchangeApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {

  @Bean
  ExchangeApi exchangeApi() {
    final WebClient client = WebClient
            .builder()
            .baseUrl("http://localhost:9000")
            .defaultStatusHandler(
                    HttpStatusCode::isError,
                    clientResponse -> Mono.just(new Exception("Account Service Error")))
            .build();
    final HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build();
    return httpServiceProxyFactory.createClient(ExchangeApi.class);
  }
}
