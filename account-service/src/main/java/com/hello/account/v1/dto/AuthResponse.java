package com.hello.account.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthResponse(
        @NotNull Long id,
        @NotBlank String name,
        boolean isValid) {

}
