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
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${distributedLock.enabled:true}")
public class DistributedMultiLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;

  @Around("@annotation(com.hello.money.config.redis.DistributedMultiLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();
    final DistributedMultiLock distributedLock = method.getAnnotation(DistributedMultiLock.class);

    final RLock[] locks = getLocks(distributedLock, signature, joinPoint);

    final RLock multiLock = redissonClient.getMultiLock(locks);
    log.info("{}:{} - 1. multiLock 생성", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());

    try {
      log.info("{}:{} - 2. multiLock 획득 시도", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());
      if (!multiLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit())) {
        log.error("{}:{} - 99. multiLock 획득 실패", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());
        throw new RedisLockAcquisitionFailedException("Failed to acquire multi lock");
      }
      log.info("{}:{} - 3. multiLock 획득 성공", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());
      return joinPoint.proceed();
    } finally {
      try {
        multiLock.unlock();
        log.info("{}:{} - 4. multiLock 해제 성공", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());
      } catch (CompletionException e) {
        log.error("{}:{} - 98. multiLock 이미 해제 완료", Thread.currentThread().getId(), Stream.of(locks).map(RLock::getName).toList());
      }
    }
  }

  private RLock[] getLocks(final DistributedMultiLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return Stream.of(distributedLock.keys())
                 .map(key -> REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), key))
                 .map(redissonClient::getLock)
                 .toArray(RLock[]::new);
  }
}
