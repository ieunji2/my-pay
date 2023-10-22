package com.hello.money.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

  private Long accountId;

  private BigInteger balance;

  @OneToMany(mappedBy = "wallet", cascade = ALL, orphanRemoval = true)
  private final List<Transaction> transactions = new ArrayList<>();

  public Wallet(final Long accountId) {
    Assert.notNull(accountId, "계정 ID는 필수입니다.");
    this.accountId = accountId;
    this.balance = BigInteger.ZERO;
  }

  public void addMoney(final BigInteger amount) {
    this.balance = this.balance.add(amount);
  }
}
