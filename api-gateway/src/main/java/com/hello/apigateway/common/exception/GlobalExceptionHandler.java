package com.hello.apigateway.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Order(-1)
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(final ServerWebExchange exchange, final Throwable ex) {
    log.error("handle", ex);

    final ServerHttpResponse response = exchange.getResponse();
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    final ErrorResponse errorResponse = getErrorResponse(ex, response);

    String error = "";
    try {
      error = objectMapper.writeValueAsString(errorResponse);
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException", e);
      exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(errorResponse.status()));
      return exchange.getResponse().setComplete();
    }

    final DataBuffer buffer = exchange.getResponse()
                                      .bufferFactory()
                                      .wrap(error.getBytes(StandardCharsets.UTF_8));

    return response.writeWith(Flux.just(buffer));
  }

  private static ErrorResponse getErrorResponse(final Throwable ex, final ServerHttpResponse response) {
    if (ex instanceof ResponseStatusException) {
      return getErrorResponse(response, ErrorCode.RESPONSE_STATUS_ERROR);
    }
    if (ex instanceof BusinessException) {
      return getErrorResponse(response, ((BusinessException) ex).getErrorCode());
    }
    return getErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
  }

  private static ErrorResponse getErrorResponse(final ServerHttpResponse response, final ErrorCode errorCode) {
    response.setStatusCode(HttpStatusCode.valueOf(errorCode.getStatus()));
    return ErrorResponse.of(errorCode);
  }
}
