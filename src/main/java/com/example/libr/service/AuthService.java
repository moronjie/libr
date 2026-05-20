package com.example.libr.service;

import com.example.libr.dto.request.*;
import com.example.libr.dto.response.AuthResponse;
import com.example.libr.dto.response.MessageResponse;
import com.example.libr.dto.response.UserDto;
import com.example.libr.entity.RefreshToken;
import com.example.libr.entity.User;
import com.example.libr.exception.AppException;
import com.example.libr.exception.ConflictException;
import com.example.libr.repository.RefreshTokenRepository;
import com.example.libr.repository.UserRepository;
import com.example.libr.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

// service/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    // ── UserDetailsService (used by JwtAuthFilter) ──────────────────────────

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    // ── Register ─────────────────────────────────────────────────────────────

    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String otp = otpService.generateAndStoreOtp(user.getEmail(), "verify");
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp, "verify");

        return new MessageResponse("Registration successful. Check your email for the OTP.");
    }

    // ── Verify Email ──────────────────────────────────────────────────────────

    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        User user = findUserByEmail(request.getEmail());

        if (user.isVerified()) {
            throw new ConflictException("Email already verified");
        }

        boolean valid = otpService.verifyOtp(request.getEmail(), "verify", request.getOtp());
        if (!valid) {
            throw new AppException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        user.setVerified(true);
        userRepository.save(user);

        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    // ── Resend OTP ────────────────────────────────────────────────────────────

    public MessageResponse resendOtp(ForgotPasswordRequest request) {
        User user = findUserByEmail(request.getEmail());

        if (user.isVerified()) {
            throw new AppException("Email already verified", HttpStatus.BAD_REQUEST);
        }

        String otp = otpService.generateAndStoreOtp(user.getEmail(), "verify");
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp, "verify");

        return new MessageResponse("OTP resent. Check your email.");
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = findUserByEmail(request.getEmail());

        if (!user.isVerified()) {
            throw new AppException("Please verify your email first", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken  = jwtUtils.generateAccessToken(user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        saveRefreshToken(user, refreshToken);
        attachRefreshTokenCookie(response, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(mapToUserDto(user))
                .build();
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshTokenFromCookie(request);

        if (!jwtUtils.isTokenValid(rawToken)) {
            throw new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        UUID userId = UUID.fromString(jwtUtils.extractSubject(rawToken));
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        RefreshToken stored = refreshTokenRepository.findAllByUser(user)
                .stream()
                .filter(t -> !t.isRevoked() && passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new AppException("Refresh token not recognised", HttpStatus.UNAUTHORIZED));

        // Rotate — revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken  = jwtUtils.generateAccessToken(user.getId());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId());

        saveRefreshToken(user, newRefreshToken);
        attachRefreshTokenCookie(response, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .user(mapToUserDto(user))
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public MessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshTokenFromCookie(request);

        if (rawToken != null && jwtUtils.isTokenValid(rawToken)) {
            UUID userId = UUID.fromString(jwtUtils.extractSubject(rawToken));
            userRepository.findById(userId).ifPresent(user ->
                    refreshTokenRepository.deleteAllByUser(user));
        }

        clearRefreshTokenCookie(response);
        return new MessageResponse("Logged out successfully.");
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = findUserByEmail(request.getEmail());

        String otp = otpService.generateAndStoreOtp(user.getEmail(), "reset");
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp, "reset");

        return new MessageResponse("If that email exists, a reset code has been sent.");
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = findUserByEmail(request.getEmail());

        boolean valid = otpService.verifyOtp(request.getEmail(), "reset", request.getOtp());
        if (!valid) {
            throw new AppException("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.deleteAllByUser(user);

        return new MessageResponse("Password reset successful. Please log in again.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
    }

    private void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(rawToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                .build();
        refreshTokenRepository.save(token);
    }

    private void attachRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge((int) (refreshTokenExpiry / 1000));
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
