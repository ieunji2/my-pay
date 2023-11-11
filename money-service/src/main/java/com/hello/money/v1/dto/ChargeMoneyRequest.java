package com.hello.money.v1.dto;

import java.math.BigInteger;

public record ChargeMoneyRequest(BigInteger amount, String summary) {
}
