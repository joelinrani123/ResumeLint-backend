package com.resumelint.dto;

/** Equivalent of the ErrorResponse schema: {@code { "error": string } }. */
public record ErrorResponseDto(String error) {
}
