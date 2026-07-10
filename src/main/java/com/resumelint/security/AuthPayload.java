package com.resumelint.security;

/**
 * Decoded JWT payload, equivalent to the Node backend's
 * {@code AuthPayload { userId: number; email: string }}.
 */
public record AuthPayload(Long userId, String email) {
}
