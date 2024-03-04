package com.hello.money.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "wallet_id")
  private Wallet wallet;

  private Long receiverWalletId;

  private BigInteger amount;

  private String summary;

  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  private TransactionStatus transactionStatus;

  public Transaction(
          final Wallet wallet,
          final Long receiverWalletId,
          final BigInteger amount,
          final String summary) {
    Assert.notNull(wallet, "지갑은 필수입니다.");
    Assert.notNull(receiverWalletId, "수취인 지갑 ID는 필수입니다.");
    Assert.isTrue(amount.compareTo(BigInteger.ZERO) > 0, "금액은 0보다 커야 합니다.");
    this.wallet = wallet;
    this.receiverWalletId = receiverWalletId;
    this.amount = amount;
    this.summary = summary;
    this.transactionType = Objects.equals(wallet.getId(), receiverWalletId) ? TransactionType.DEPOSIT : TransactionType.WITHDRAW;
    this.transactionStatus = TransactionStatus.REQUEST;
  }

  public Transaction success() {
    this.transactionStatus = TransactionStatus.NORMAL;
    return this;
  }

  public Transaction fail() {
    this.transactionStatus = TransactionStatus.ERROR;
    return this;
  }
}
