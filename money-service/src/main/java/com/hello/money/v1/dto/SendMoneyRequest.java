package com.hello.money.v1.dto;

import java.math.BigInteger;

public record SendMoneyRequest(Long receiverWalletId, BigInteger amount, String summary) {
}
