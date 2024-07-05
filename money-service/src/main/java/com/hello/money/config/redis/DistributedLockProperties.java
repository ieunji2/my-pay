package com.hello.money.config.redis;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.TimeUnit;

@Slf4j
@Validated
@ConfigurationProperties(prefix = "distributed.lock")
public record DistributedLockProperties(
        @NotNull long waitTime,
        @NotNull long leaseTime,
        @NotNull TimeUnit timeUnit) {

  @PostConstruct
  public void init() {
    log.info("distributed.lock.waitTime={}", waitTime);
    log.info("distributed.lock.leaseTime={}", leaseTime);
    log.info("distributed.lock.timeUnit={}", timeUnit);
  }
}
