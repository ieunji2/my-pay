package com.hello.money.config;

import com.hello.money.v1.controller.MoneyController;
import com.hello.money.v1.dto.Account;
import com.hello.money.v1.service.MoneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@WebMvcTest(MoneyController.class)
class AccountArgumentResolverTest {

  @MockBean
  private MoneyService moneyService;

  @Mock
  private MethodParameter parameter;

  @Mock
  private NativeWebRequest webRequest;

  private AccountArgumentResolver accountArgumentResolver;

  @BeforeEach
  void setUp() {
    accountArgumentResolver = new AccountArgumentResolver();
  }

  @Test
  void Account객체_반환하는지_확인() throws Exception {
    //given
    final Long accountId = 1L;
    final String accountName = "이름";

    when(webRequest.getHeader("x-account-id"))
            .thenReturn(encode(String.valueOf(accountId)));
    when(webRequest.getHeader("x-account-name"))
            .thenReturn(encode(accountName));

    //when
    final Account account = (Account) accountArgumentResolver.resolveArgument(
            parameter,
            null,
            webRequest,
            null);

    //then
    assertThat(account).isEqualTo(new Account(accountId, accountName));
  }

  @Test
  void 요청_헤더에_값이_없으면_오류() {
    //given
    when(webRequest.getHeader("x-account-id"))
            .thenReturn("");

    //when, then
    assertThatThrownBy(() -> {
      accountArgumentResolver.resolveArgument(
              parameter,
              null,
              webRequest,
              null);
    }).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("잘못된 요청입니다.");
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}