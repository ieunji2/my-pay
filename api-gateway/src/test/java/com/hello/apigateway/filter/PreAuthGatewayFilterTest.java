package com.hello.apigateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PreAuthGatewayFilterTest {

  private static Stream<Arguments> headerValues() {
    return Stream.of(
            arguments(1L, "이름")
    );
  }

  @ParameterizedTest
  @MethodSource("headerValues")
  @DisplayName("헤더 변경은 잘 된다")
  void mutateHttpRequestHeader(final Long id, final String name) {
    //given
    final MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest
                    .get("/path")
                    .build());

    assertThat(exchange.getRequest().getHeaders()).isEmpty();

    //when
    exchange.getRequest().mutate()
            .header("x-account-id", String.valueOf(id))
            .header("x-account-name", name)
            .build();

    //then
    assertThat(exchange.getRequest().getHeaders().get("x-account-id")).contains(String.valueOf(id));
    assertThat(exchange.getRequest().getHeaders().get("x-account-name")).contains(name);
  }
}