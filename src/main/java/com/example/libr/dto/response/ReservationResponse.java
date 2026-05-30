package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReservationResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID bookId;
    private String bookTitle;
    private String bookAuthor;
    private String status;
    private int queuePosition;
    private LocalDateTime readyAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}