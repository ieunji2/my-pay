package com.hello.account.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String email;

  private boolean isValid;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(updatable = false)
  private LocalDateTime updatedAt;


  public Account(final String name, final String email) {
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
    this.name = name;
    this.email = email;
    this.isValid = true;
  }

  public void modify(final String name, final String email, final boolean isValid) {
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
    this.name = name;
    this.email = email;
    this.isValid = isValid;
  }
}
