package com.resumelint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Equivalent of the Node SignupBody zod schema. */
public record SignupRequest(

        @NotBlank(message = "name is required")
        @Size(min = 2, message = "name must contain at least 2 character(s)")
        String name,

        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must contain at least 8 character(s)")
        String password
) {
}
