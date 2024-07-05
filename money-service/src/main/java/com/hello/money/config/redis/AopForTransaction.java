package com.hello.money.config.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AopForTransaction {

  private static final int TRANSACTION_TIMEOUT = 4;

  private final DistributedLockProperties properties;

  @PostConstruct
  public void validateTimeout() {
    if (TRANSACTION_TIMEOUT > properties.leaseTime()) {
      throw new IllegalArgumentException("분산락 내에서 동작하는 트랜잭션의 timeout은 leaseTime보다 작아야 합니다.");
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = TRANSACTION_TIMEOUT)
  public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
    return joinPoint.proceed();
  }
}
