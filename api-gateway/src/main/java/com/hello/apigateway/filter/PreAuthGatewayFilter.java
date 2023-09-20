package com.hello.apigateway.filter;

import com.hello.apigateway.dto.ResponseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PreAuthGatewayFilter extends AbstractGatewayFilterFactory<PreAuthGatewayFilter.Config> {

  private static final String AUTH_URL = "http://localhost:9000/v1/auth/check";

  private final RestTemplate restTemplate;

  public PreAuthGatewayFilter(final RestTemplate restTemplate) {
    super(Config.class);
    this.restTemplate = restTemplate;
  }

  @Override
  public GatewayFilter apply(final Config config) {

    return (exchange, chain) -> {
      log.info("Request Id -> {}", exchange.getRequest().getId());

      final String token = getToken(exchange);
      final ResponseAuth responseAuth = verifyToken(exchange, token);
      rebuildRequestHeader(exchange, responseAuth);

      return exchange.getResponse().isCommitted() ? setResponseComplete(exchange) : chain.filter(exchange);
    };
  }

  private String getToken(final ServerWebExchange exchange) {

    final HttpHeaders headers = exchange.getRequest().getHeaders();

    if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
      onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
    }

    final String bearer = "Bearer ";
    if (!headers.get(HttpHeaders.AUTHORIZATION).get(0).startsWith(bearer)) {
      onError(exchange, "Not a valid token format", HttpStatus.UNAUTHORIZED);
    }

    return headers.get(HttpHeaders.AUTHORIZATION).get(0).replace(bearer, "");
  }

  private ResponseAuth verifyToken(final ServerWebExchange exchange, final String token) {

    final ResponseEntity<ResponseAuth> responseEntity = getExchange(token);

    if (responseEntity.getStatusCode().isError()) {
      onError(exchange, "HTTP communication error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    final ResponseAuth responseAuth = responseEntity.getBody();
    if (!responseAuth.isValid()) {
      onError(exchange, "Authentication token invalid", HttpStatus.UNAUTHORIZED);
    }

    return responseAuth;
  }

  private ResponseEntity<ResponseAuth> getExchange(final String token) {
    return restTemplate.exchange(
            AUTH_URL,
            HttpMethod.GET,
            getAuthHeaderEntity(token),
            ResponseAuth.class);
  }

  private HttpEntity<?> getAuthHeaderEntity(final String token) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return new HttpEntity<>(headers);
  }

  private void rebuildRequestHeader(final ServerWebExchange exchange, final ResponseAuth responseAuth) {
    removeAuthorizationHeader(exchange);
    setAuthenticatedUserHeader(exchange, responseAuth);
  }

  private void removeAuthorizationHeader(final ServerWebExchange exchange) {
    final HttpHeaders headers = HttpHeaders.writableHttpHeaders(exchange.getRequest().getHeaders());
    headers.remove(HttpHeaders.AUTHORIZATION);
  }

  private void setAuthenticatedUserHeader(final ServerWebExchange exchange, final ResponseAuth responseAuth) {
    exchange.getRequest()
            .mutate()
            .header("x-account-id", encode(String.valueOf(responseAuth.id())))
            .header("x-account-name", encode(responseAuth.name()))
            .build();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private void onError(final ServerWebExchange exchange, final String errorMessage, final HttpStatus httpStatus) {
    log.error("Error -> {}, {}", httpStatus, errorMessage);

    setResponseStatusCode(exchange, httpStatus);
    setResponseComplete(exchange);
  }

  private void setResponseStatusCode(final ServerWebExchange exchange, final HttpStatus httpStatus) {
    exchange.getResponse()
            .setStatusCode(httpStatus);
  }

  private Mono<Void> setResponseComplete(final ServerWebExchange exchange) {
    return exchange.getResponse()
                   .setComplete();
  }

  public static class Config {
  }
}
