package com.hello.apigateway.filter;

import com.hello.apigateway.dto.ResponseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PreAuthGatewayFilter extends AbstractGatewayFilterFactory<PreAuthGatewayFilter.Config> {

  private final RestTemplate restTemplate;

  public PreAuthGatewayFilter(final RestTemplate restTemplate) {
    super(Config.class);
    this.restTemplate = restTemplate;
  }

  @Override
  public GatewayFilter apply(final Config config) {

    return (exchange, chain) -> {

      final ServerHttpRequest request = exchange.getRequest();
      log.info("request Id -> {}", request.getId());

      if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
      }

      final String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
      if (!authorizationHeader.startsWith("Bearer ")) {
        return onError(exchange, "not a valid token format", HttpStatus.UNAUTHORIZED);
      }

      final ResponseEntity<ResponseAuth> responseEntity = restTemplate.exchange(
              "http://localhost:9000/v1/auth/check",
              HttpMethod.GET,
              getAuthHeaderEntity(authorizationHeader),
              ResponseAuth.class);

      if (responseEntity.getStatusCode().isError()) {
        return onError(exchange, "http communication error", HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        final ResponseAuth responseAuth = responseEntity.getBody();

        if (!responseAuth.isValid()) {
          return onError(exchange, "auth token is not valid", HttpStatus.UNAUTHORIZED);
        }

        final String encodedId = URLEncoder.encode(String.valueOf(responseAuth.id()), StandardCharsets.UTF_8);
        final String encodedName = URLEncoder.encode(responseAuth.name(), StandardCharsets.UTF_8);
        exchange.getRequest().mutate()
                .header("x-account-id", encodedId)
                .header("x-account-name", encodedName)
                .build();
      }

      return chain.filter(exchange);
    };
  }

  private HttpEntity getAuthHeaderEntity(final String authorizationHeader) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(authorizationHeader.replace("Bearer ", ""));
    return new HttpEntity<>(headers);
  }

  private Mono<Void> onError(final ServerWebExchange exchange, final String errorMessage, final HttpStatus httpStatus) {
    log.error("onError -> {}, {}", httpStatus, errorMessage);
    final ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    return response.setComplete();
  }

  public static class Config {
  }
}
