package com.hello.apigateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppConfigTest {

  AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

  @Test
  @DisplayName("모든 빈 출력하기")
  void findAllBean() {
    final String[] beanDefinitionNames = ac.getBeanDefinitionNames();
    for (String beanDefinitionName : beanDefinitionNames) {
      final Object bean = ac.getBean(beanDefinitionName);
      System.out.println("name = " + beanDefinitionName + " object = " + bean);
    }
  }

  @Test
  @DisplayName("직접 등록한 애플리케이션 빈 출력하기")
  void findApplicationBean() {
    final String[] beanDefinitionNames = ac.getBeanDefinitionNames();
    for (String beanDefinitionName : beanDefinitionNames) {
      final BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
      if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
        final Object bean = ac.getBean(beanDefinitionName);
        System.out.println("name = " + beanDefinitionName + " object = " + bean);
      }
    }
  }

  @Test
  @DisplayName("빈 이름으로 조회")
  void findBeanByName() {
    final RestTemplate restTemplate = ac.getBean("restTemplate", RestTemplate.class);
    assertThat(restTemplate).isInstanceOf(RestTemplate.class);
  }

  @Test
  @DisplayName("등록되지 않은 빈 이름으로 조회 시 오류")
  void findBeanByOtherName() {
    assertThatThrownBy(() -> {
      ac.getBean("getRestTemplate", RestTemplate.class);
    }).isInstanceOf(NoSuchBeanDefinitionException.class);
  }
}