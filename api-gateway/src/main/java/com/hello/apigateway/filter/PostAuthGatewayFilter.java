package com.hello.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PostAuthGatewayFilter extends AbstractGatewayFilterFactory<PostAuthGatewayFilter.Config> {

  public PostAuthGatewayFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
      final ServerHttpResponse response = exchange.getResponse();

      log.info("response code -> {}", response.getStatusCode());
    }));
  }

  public static class Config {
  }
}
