package com.hello.apigateway.filter;

import com.hello.apigateway.common.exception.CommunicationException;
import com.hello.apigateway.common.exception.UnauthorizedException;
import com.hello.apigateway.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class PreAuthGatewayFilter extends AbstractGatewayFilterFactory<PreAuthGatewayFilter.Config> {

  private static final String BEARER = "Bearer ";
  private static final String X_ACCOUNT_ID = "x-account-id";
  private static final String X_ACCOUNT_NAME = "x-account-name";

  @Value("${app.auth-check-url}")
  private String authCheckUrl;

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
      final AuthResponse authResponse = verifyToken(exchange, token);
      final ServerHttpRequest request = rebuildRequestHeader(exchange, authResponse);
      checkRequestHeader(request);
      return chain.filter(exchange);
    };
  }

  private static String getToken(final ServerWebExchange exchange) {
    final HttpHeaders headers = exchange.getRequest().getHeaders();
    if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
      throw new UnauthorizedException("No authorization header");
    }
    if (!headers.get(HttpHeaders.AUTHORIZATION).get(0).startsWith(BEARER)) {
      throw new UnauthorizedException("Not a valid token format");
    }
    return headers.get(HttpHeaders.AUTHORIZATION).get(0).replace(BEARER, "");
  }

  private AuthResponse verifyToken(final ServerWebExchange exchange, final String token) {
    final ResponseEntity<AuthResponse> responseEntity = getExchange(token);
    if (responseEntity.getStatusCode().isError()) {
      throw new CommunicationException();
    }
    final AuthResponse authResponse = responseEntity.getBody();
    if (!authResponse.isValid()) {
      throw new UnauthorizedException("Authentication token invalid");
    }
    return authResponse;
  }

  private ResponseEntity<AuthResponse> getExchange(final String token) {
    return restTemplate.exchange(
            authCheckUrl,
            HttpMethod.GET,
            getAuthHeaderEntity(token),
            AuthResponse.class);
  }

  private HttpEntity<?> getAuthHeaderEntity(final String token) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return new HttpEntity<>(headers);
  }

  private static ServerHttpRequest rebuildRequestHeader(final ServerWebExchange exchange, final AuthResponse authResponse) {
    final List<String> removedAuthorizationHeader = removeAuthorizationHeader(exchange);
    if (removedAuthorizationHeader.isEmpty()) {
      throw new UnauthorizedException("No authorization header");
    }
    return setAuthenticatedUserHeader(exchange, authResponse);
  }

  private static List<String> removeAuthorizationHeader(final ServerWebExchange exchange) {
    final HttpHeaders headers = HttpHeaders.writableHttpHeaders(exchange.getRequest().getHeaders());
    return headers.remove(HttpHeaders.AUTHORIZATION);
  }

  private static ServerHttpRequest setAuthenticatedUserHeader(final ServerWebExchange exchange, final AuthResponse authResponse) {
    return exchange.getRequest()
                   .mutate()
                   .header(X_ACCOUNT_ID, encode(String.valueOf(authResponse.id())))
                   .header(X_ACCOUNT_NAME, encode(authResponse.name()))
                   .build();
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static boolean checkRequestHeader(final ServerHttpRequest request) {
    if (!request.getHeaders().containsKey(X_ACCOUNT_ID) || !request.getHeaders().containsKey(X_ACCOUNT_NAME)) {
      throw new UnauthorizedException();
    }
    return true;
  }

  public static class Config {
  }
}
