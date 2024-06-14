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
public class CustomDistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;
  private final AopForTransaction aopForTransaction;

  @Around("@annotation(com.hello.money.config.redis.CustomDistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();
    final CustomDistributedLock distributedLock = method.getAnnotation(CustomDistributedLock.class);

    final RLock lock = isOne(distributedLock) ? getLock(distributedLock, signature, joinPoint) : getLocks(distributedLock, signature, joinPoint);

    try {
      if (!lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit())) {
        throw new RedisLockAcquisitionFailedException("Failed to acquire lock");
      }
      return aopForTransaction.proceed(joinPoint);
    } finally {
      try {
        lock.unlock();
      } catch (CompletionException e) {
        log.error(e.getMessage());
      }
    }
  }

  private static boolean isOne(final CustomDistributedLock distributedLock) {
    return distributedLock.keys().length == 1;
  }

  private RLock getLock(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return redissonClient.getLock(
            REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.keys()[0]));
  }

  private RLock getLocks(final CustomDistributedLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    return redissonClient.getMultiLock(
            Stream.of(distributedLock.keys())
                  .map(key -> CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), key))
                  .map(key -> Long.parseLong(String.valueOf(key)))
                  .sorted()
                  .map(key -> redissonClient.getLock(REDISSON_LOCK_PREFIX + key))
                  .toArray(RLock[]::new));
  }
}
