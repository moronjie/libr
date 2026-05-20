package com.example.libr.service.impl;

public interface IEmailService {
    public void sendOtpEmail(String to, String name, String otp, String purpose);
}
