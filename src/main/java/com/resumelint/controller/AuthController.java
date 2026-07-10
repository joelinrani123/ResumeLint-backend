package com.resumelint.controller;

import com.resumelint.dto.*;
import com.resumelint.security.CurrentUser;
import com.resumelint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Equivalent of routes/auth.ts. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResultDto signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResultDto login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public MessageResponseDto logout() {
        return new MessageResponseDto("Logged out");
    }

    /** Protected via AuthInterceptor (see WebConfig). */
    @GetMapping("/me")
    public UserDto me() {
        return authService.getCurrentUser(currentUser.get().userId());
    }
}
