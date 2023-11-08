package com.hello.money.v1.dto;

import java.math.BigInteger;

public record ChargeMoneyServiceDto(
        Long accountId,
        String accountName,
        BigInteger amount,
        String summary) {
}
