package com.resumelint.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelint.dto.ErrorResponseDto;
import com.resumelint.security.AuthPayload;
import com.resumelint.security.CurrentUser;
import com.resumelint.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Direct equivalent of {@code requireAuth} in the Node backend's
 * {@code middlewares/auth.ts}. Applied only to the routes that used it:
 * {@code /auth/me}, {@code /resumes/**}, {@code /dashboard/**} (see
 * {@link com.resumelint.config.WebConfig}).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Unauthorized");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            AuthPayload payload = jwtService.verifyToken(token);
            request.setAttribute(CurrentUser.REQUEST_ATTRIBUTE, payload);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "Invalid or expired token");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponseDto(message)));
    }
}
