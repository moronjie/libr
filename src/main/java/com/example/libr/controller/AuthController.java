package com.example.libr.controller;

import com.example.libr.dto.request.*;
import com.example.libr.dto.response.AuthResponse;
import com.example.libr.dto.response.MessageResponse;
import com.example.libr.payload.ApiResponse;
import com.example.libr.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<MessageResponse>builder()
                        .message("Registration successful")
                        .data(response)
                        .build());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request);
        return ResponseEntity
                .ok(ApiResponse.<MessageResponse>builder()
                        .message(response.getMessage())
                        .data(null)
                        .build());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<MessageResponse>> resendOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.resendOtp(request);
        return ResponseEntity
                .ok(ApiResponse.<MessageResponse>builder()
                        .message(response.getMessage())
                        .data(null)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity
                .ok(ApiResponse.<AuthResponse>builder()
                        .message("Login successful")
                        .data(authResponse)
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.refresh(request, response);
        return ResponseEntity
                .ok(ApiResponse.<AuthResponse>builder()
                        .message("Token refreshed successfully")
                        .data(authResponse)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<MessageResponse>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        MessageResponse messageResponse = authService.logout(request, response);
        return ResponseEntity
                .ok(ApiResponse.<MessageResponse>builder()
                        .message(messageResponse.getMessage())
                        .data(null)
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<MessageResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity
                .ok(ApiResponse.<MessageResponse>builder()
                        .message(response.getMessage())
                        .data(null)
                        .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.<MessageResponse>builder()
                        .message(response.getMessage())
                        .data(null)
                        .build());
    }
}
