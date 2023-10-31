package com.hello.money.v1.facade;


import com.hello.money.v1.dto.Account;
import com.hello.money.v1.dto.SaveMoneyRequest;
import com.hello.money.v1.dto.SendMoneyRequest;
import com.hello.money.v1.dto.WalletResponse;
import com.hello.money.v1.service.MoneyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedissonLockMoneyFacade {

  private final RedissonClient redissonClient;

  private final MoneyService moneyService;

  private static final String redisson_lock_prefix = "LOCK:SERVICE:MONEY:";
  private static final int waitTime = 30;
  private static final int leaseTime = 1;
  private static final TimeUnit timeUnit = TimeUnit.SECONDS;

  @Getter
  private int unavailableSaveMoneyCount = 0;

  @Getter
  private int unavailableSendMoneyCount = 0;

  public WalletResponse saveMoney(final Account account, final SaveMoneyRequest request) {

    //TODO walletId 가져오기
    final RLock lock = redissonClient.getLock(redisson_lock_prefix + "walletId");

    try {
      final boolean available = lock.tryLock(waitTime, leaseTime, timeUnit);

      if (!available) {
        ++unavailableSaveMoneyCount;
        throw new RuntimeException("lock 획득 실패");
      }

      return moneyService.saveMoney(account, request);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  public WalletResponse sendMoney(final Account account, final SendMoneyRequest request) {

    //TODO senderWalletId, receiverWalletId 가져오기
    final RLock lock1 = redissonClient.getLock(redisson_lock_prefix + "senderWalletId");
    final RLock lock2 = redissonClient.getLock(redisson_lock_prefix + "receiverWalletId");

    final RLock[] locks = {lock1, lock2};

    final RLock multiLock = redissonClient.getMultiLock(locks);

    try {
      final boolean available = multiLock.tryLock(waitTime, waitTime, timeUnit);

      if (!available) {
        ++unavailableSendMoneyCount;
        throw new RuntimeException("lock 획득 실패");
      }

      return moneyService.sendMoney(account, request);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      multiLock.unlock();
    }
  }
}
