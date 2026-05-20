package com.example.libr.service;

import com.example.libr.exception.AppException;
import com.example.libr.service.impl.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendOtpEmail(String to, String name, String otp, String purpose) {
        String subject = purpose.equals("verify")
                ? "Verify your email"
                : "Reset your password";

        String body = buildOtpEmailBody(name, otp, purpose);
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new AppException("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildOtpEmailBody(String name, String otp, String purpose) {
        String action = purpose.equals("verify") ? "verify your email" : "reset your password";
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Hello, %s</h2>
                    <p>Use the code below to %s. It expires in <strong>10 minutes</strong>.</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                                padding: 16px; background: #f4f4f4; text-align: center;
                                border-radius: 8px;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 12px;">
                        If you didn't request this, you can safely ignore this email.
                    </p>
                </div>
                """.formatted(name, action, otp);
    }
}
