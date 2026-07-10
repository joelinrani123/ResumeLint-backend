package com.resumelint.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Formats an {@link Instant} exactly like JavaScript's
 * {@code Date.prototype.toISOString()} — always UTC, always exactly 3
 * fractional-second digits, e.g. {@code 2024-01-01T00:00:00.000Z} — so JSON
 * payloads are byte-for-byte identical to the original Node backend.
 */
public final class IsoDates {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private IsoDates() {
    }

    public static String toIso(Instant instant) {
        return FORMATTER.format(instant);
    }

    /** Equivalent of {@code date.toISOString().split("T")[0]}. */
    public static String toDateOnly(Instant instant) {
        return DATE_FORMATTER.format(instant);
    }
}
