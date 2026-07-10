package com.resumelint.dto;

import jakarta.validation.constraints.NotBlank;

/** Equivalent of the Node LoginBody zod schema. */
public record LoginRequest(
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password
) {
}
