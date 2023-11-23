package com.hello.account.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterAccountRequest(
        @NotBlank String name,
        @Email String email) {

}

