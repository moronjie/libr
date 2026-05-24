package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MemberResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String nationalId;
    private String profileImageUrl;
    private String tier;
    private int maxBorrowLimit;
    private int fineGraceDays;
    private String status;
    private boolean profileComplete;
    private LocalDate membershipExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}