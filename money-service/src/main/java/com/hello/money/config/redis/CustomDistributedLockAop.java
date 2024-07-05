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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${distributedLock.enabled:true}")
public class CustomDistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;
  private final AopForTransaction aopForTransaction;
  private final DistributedLockProperties properties;

  @Around("@annotation(com.hello.money.config.redis.CustomDistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();
    final CustomDistributedLock distributedLock = method.getAnnotation(CustomDistributedLock.class);

    final RLock lock = isSingleKey(distributedLock) ? getLock(distributedLock, signature, joinPoint) : getMultiLock(distributedLock, signature, joinPoint);

    final List<String> lockNames = getLockNames(distributedLock, signature, joinPoint, lock);

    try {
      logLockInfo("1. RLock 획득 시도", lockNames);
      if (!lock.tryLock(properties.waitTime(), properties.leaseTime(), properties.timeUnit())) {
        logLockInfo("99. RLock 획득 실패", lockNames);
        throw new RedisLockAcquisitionFailedException("Failed to acquire lock");
      }
      logLockInfo("2. RLock 획득 성공", lockNames);
      return aopForTransaction.proceed(joinPoint);
    } finally {
      try {
        lock.unlock();
        logLockInfo("3. RLock 해제 성공", lockNames);
      } catch (CompletionException e) {
        logLockInfo("98. RLock 이미 해제 완료", lockNames);
        log.error(e.getMessage());
      }
    }
  }

  private static boolean isSingleKey(final CustomDistributedLock distributedLock) {
    if (distributedLock.keys().length == 0) {
      throw new RuntimeException("분산락의 키는 적어도 한 개 이상 지정해야 합니다.");
    }
    return distributedLock.keys().length == 1;
  }

  private List<String> getLockNames(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint, final RLock lock) {
    if (isSingleKey(distributedLock)) {
      return Collections.singletonList(lock.getName());
    }
    return stream(getLocks(distributedLock, signature, joinPoint)).map(RLock::getName).toList();
  }

  private RLock getLock(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return redissonClient.getLock(
            REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.keys()[0]));
  }

  private RLock getMultiLock(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return redissonClient.getMultiLock(
            getLocks(distributedLock, signature, joinPoint));
  }

  private RLock[] getLocks(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return Stream.of(distributedLock.keys())
                 .map(key -> CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), key))
                 .map(key -> Long.parseLong(String.valueOf(key)))
                 .sorted()
                 .map(key -> redissonClient.getLock(REDISSON_LOCK_PREFIX + key))
                 .toArray(RLock[]::new);
  }

  private void logLockInfo(final String message, final List<String> lockNames) {
    log.info("{}:{} - {}", Thread.currentThread().getId(), lockNames, message);
  }
}