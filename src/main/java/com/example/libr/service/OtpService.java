package com.example.libr.service;

import com.example.libr.service.impl.IOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiration}")
    private long otpExpiry;

    private static final String VERIFY_PREFIX = "otp:verify:";
    private static final String RESET_PREFIX  = "otp:reset:";

    public String generateAndStoreOtp(String email, String type) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        String key = buildKey(email, type);
        String hashed = passwordEncoder.encode(otp);

        if (hashed != null) {
            redisTemplate.opsForValue().set(key, hashed, otpExpiry, TimeUnit.SECONDS);
        }
        log.info("Generated OTP for type={}, email={}, otp={}", type, email, otp);
        return otp;
    }

    public boolean verifyOtp(String email, String type, String rawOtp) {
        String key = buildKey(email, type);
        String storedHash = redisTemplate.opsForValue().get(key);

        if (storedHash == null) return false;

        boolean matches = passwordEncoder.matches(rawOtp, storedHash);
        if (matches) redisTemplate.delete(key);
        return matches;
    }

    private String buildKey(String email, String type) {
        return switch (type) {
            case "verify" -> VERIFY_PREFIX + email;
            case "reset"  -> RESET_PREFIX + email;
            default -> throw new IllegalArgumentException("Unknown OTP type: " + type);
        };
    }
}