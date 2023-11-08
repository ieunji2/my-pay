package com.hello.money.v1.dto;

import java.math.BigInteger;

public record SendMoneyServiceDto(
        Long accountId,
        String accountName,
        Long receiverWalletId,
        BigInteger amount,
        String summary) {
}
