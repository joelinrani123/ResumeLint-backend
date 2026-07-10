package com.resumelint.service;

import com.resumelint.dto.AuthResultDto;
import com.resumelint.dto.LoginRequest;
import com.resumelint.dto.SignupRequest;
import com.resumelint.dto.UserDto;
import com.resumelint.entity.User;
import com.resumelint.exception.ApiException;
import com.resumelint.repository.UserRepository;
import com.resumelint.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Equivalent of routes/auth.ts: signup, login, me lookups. Passwords are
 * hashed with BCrypt (strength 10) and tokens are signed HS256 JWTs valid
 * for 7 days, matching the Node implementation exactly.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResultDto signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), "Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        String token = jwtService.signToken(user.getId(), user.getEmail());
        return new AuthResultDto(toDto(user), token);
    }

    @Transactional(readOnly = true)
    public AuthResultDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password");
        }

        String token = jwtService.signToken(user.getId(), user.getEmail());
        return new AuthResultDto(toDto(user), token);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "User not found"));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                IsoDates.toIso(user.getCreatedAt())
        );
    }
}
