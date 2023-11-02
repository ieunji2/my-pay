package com.hello.money.config.auth;

import com.hello.money.v1.dto.Account;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class AccountArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(final MethodParameter parameter) {
    return parameter.hasParameterAnnotation(Authenticated.class) &&
            Account.class.equals(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
          final MethodParameter parameter,
          final ModelAndViewContainer mavContainer,
          final NativeWebRequest webRequest,
          final WebDataBinderFactory binderFactory) throws Exception {

    final XAccount xAccount = getXAccount(webRequest);

    return new Account(Long.valueOf(decode(xAccount.id())), decode(xAccount.name()));
  }

  private XAccount getXAccount(final NativeWebRequest webRequest) {
    final String xAccountId = webRequest.getHeader("x-account-id");
    final String xAccountName = webRequest.getHeader("x-account-name");

    checkRequestHeaderValue(xAccountId, xAccountName);

    return new XAccount(xAccountId, xAccountName);
  }

  private static void checkRequestHeaderValue(final String xAccountId, final String xAccountName) {
    if (!StringUtils.hasText(xAccountId) || !StringUtils.hasText(xAccountName)) {
      throw new IllegalArgumentException("잘못된 요청입니다.");
    }
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }
}
