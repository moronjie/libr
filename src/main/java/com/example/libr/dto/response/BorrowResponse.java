package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BorrowResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID bookId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;
    private String status;
    private boolean isOverdue;
    private long overdueDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}