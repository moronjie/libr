package com.example.libr.service.impl;

import com.example.libr.dto.request.BorrowRequest;
import com.example.libr.dto.response.BorrowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IBorrowService {
    BorrowResponse borrowBook(UUID userId, BorrowRequest request);
    BorrowResponse returnBook(UUID userId, UUID borrowRecordId);
    Page<BorrowResponse> getMyBorrowHistory(UUID userId, Pageable pageable);
    Page<BorrowResponse> getMyActiveBorrows(UUID userId, Pageable pageable);
    Page<BorrowResponse> getAllBorrowRecords(Pageable pageable);
    Page<BorrowResponse> getBookBorrowHistory(UUID bookId, Pageable pageable);
    BorrowResponse getBorrowRecord(UUID borrowRecordId);
    BorrowResponse adminReturnBook(UUID borrowRecordId);
    void markOverdueRecords();
}
