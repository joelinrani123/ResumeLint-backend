package com.resumelint.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Convenience accessor for the {@link AuthPayload} attached to the current
 * request by {@link com.resumelint.filter.AuthInterceptor}. Equivalent to
 * reading {@code req.auth} in the Express middleware.
 */
@Component
public class CurrentUser {

    public static final String REQUEST_ATTRIBUTE = "auth";

    public AuthPayload get() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No active request context");
        }
        HttpServletRequest request = attrs.getRequest();
        AuthPayload payload = (AuthPayload) request.getAttribute(REQUEST_ATTRIBUTE);
        if (payload == null) {
            throw new IllegalStateException("Request is not authenticated");
        }
        return payload;
    }
}
