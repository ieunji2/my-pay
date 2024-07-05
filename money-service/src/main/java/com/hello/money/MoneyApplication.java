package com.hello.money;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoneyApplication {
  public static void main(String[] args) {
    SpringApplication.run(MoneyApplication.class, args);
  }
}