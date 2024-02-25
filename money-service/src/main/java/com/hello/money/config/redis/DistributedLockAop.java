package com.hello.money.config.redis;

import com.hello.money.common.exception.RedisLockAcquisitionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${distributedLock.enabled:true}")
public class DistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;

  @Around("@annotation(com.hello.money.config.redis.DistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();
    final DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    final String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
            signature.getParameterNames(),
            joinPoint.getArgs(),
            distributedLock.key());

    final RLock lock = redissonClient.getLock(key);
    log.info("{}:{} - 1. lock 생성", Thread.currentThread().getId(), lock.getName());

    try {
      log.info("{}:{} - 2. lock 획득 시도", Thread.currentThread().getId(), lock.getName());
      if (!lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit())) {
        log.info("{}:{} - 99. lock 획득 실패", Thread.currentThread().getId(), lock.getName());
        throw new RedisLockAcquisitionFailedException("Failed to acquire lock");
      }
      log.info("{}:{} - 3. lock 획득 성공", Thread.currentThread().getId(), lock.getName());
      return joinPoint.proceed();
    } finally {
      if (lock.isLocked() && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.info("{}:{} - 4. lock 해제 성공", Thread.currentThread().getId(), lock.getName());
      }
    }
  }
}
