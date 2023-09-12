package com.hello.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

  private String name;

  private String email;

  private boolean isValid;

  public Account(final String name, final String email) {
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
    this.name = name;
    this.email = email;
    this.isValid = true;
  }

  public void updateAccount(final String name, final String email, final boolean isValid) {
    Assert.hasText(name, "이름은 필수입니다.");
    Assert.hasText(email, "이메일은 필수입니다.");
    this.name = name;
    this.email = email;
    this.isValid = isValid;
  }
}
