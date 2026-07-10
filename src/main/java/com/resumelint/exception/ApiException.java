package com.resumelint.exception;

/**
 * Thrown by services/controllers to short-circuit with a specific HTTP
 * status and an {"error": message} JSON body, equivalent to the ad-hoc
 * {@code res.status(x).json({ error: ... })} calls scattered throughout the
 * Node route handlers.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
