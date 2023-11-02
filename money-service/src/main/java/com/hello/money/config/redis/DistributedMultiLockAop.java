package com.hello.money.config.redis;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
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

    try {
      if (!multiLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit())) {
        throw new RuntimeException("Failed to acquire multi lock");
      }

      return joinPoint.proceed();
    } finally {
      multiLock.unlock();
    }
  }

  private RLock[] getLocks(final DistributedMultiLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    RLock[] locks = {};
    final String[] keys = getKeys(distributedLock, signature, joinPoint);
    for (String key : keys) {
      locks = appendElement(locks, redissonClient.getLock(key));
    }
    return locks;
  }

  private String[] getKeys(final DistributedMultiLock distributedLock, final MethodSignature signature, final ProceedingJoinPoint joinPoint) {
    String[] keys = {};
    for (String key : distributedLock.keys()) {
      keys = appendElement(keys, REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
              signature.getParameterNames(),
              joinPoint.getArgs(),
              key));
    }
    return keys;
  }

  private <T> T[] appendElement(T[] originArray, T element) {
    final T[] newArray = Arrays.copyOf(originArray, originArray.length + 1);
    newArray[originArray.length] = element;
    return newArray;
  }
}
