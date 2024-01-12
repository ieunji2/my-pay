package com.hello.account.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record ModifyAccountRequest(
        @NotBlank String name,
        @NotBlank String email,
        boolean isValid) {

}
