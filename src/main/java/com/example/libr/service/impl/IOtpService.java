package com.example.libr.service.impl;

public interface IOtpService {
    public String generateAndStoreOtp(String email, String type);
    public boolean verifyOtp(String email, String type, String rawOtp);
}
